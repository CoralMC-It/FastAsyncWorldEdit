package it.coralmc.fawepatch;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/** Exact-baseline assembly: replaces only trim(Z)Z, leaving all other adapters/protocols unchanged. */
public final class AssembleTrimFix {
    private static final String BASELINE = "5555bd629b8bf96e513bea704c5f8659858ad83f8722c1137eae4b141c18f09d";
    private static final String TARGET = "com/sk89q/worldedit/bukkit/adapter/impl/fawe/v26_2/PaperweightGetBlocks";
    private static final String DONOR = "it/coralmc/fawepatch/TrimMethod";
    public static void main(String[] args) throws Exception {
        Path input = Path.of(args[0]), classes = Path.of(args[1]), output = Path.of(args[2]);
        String digest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(input)));
        if (!BASELINE.equals(digest)) throw new IllegalArgumentException("Unrecognized FAWE baseline: " + digest);
        ClassNode donor = new ClassNode();
        new ClassReader(Files.readAllBytes(classes.resolve(DONOR + ".class"))).accept(donor, 0);
        MethodNode replacement = donor.methods.stream().filter(m -> m.name.equals("trim") && m.desc.equals("(Z)Z")).findFirst().orElseThrow();
        replacement.localVariables = null;
        int patched = 0, unchanged = 0;
        Files.createDirectories(output.getParent());
        try (ZipInputStream in = new ZipInputStream(Files.newInputStream(input)); ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(output))) {
            ZipEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                byte[] bytes = in.readAllBytes();
                if (entry.getName().equals(TARGET + ".class")) {
                    ClassNode target = new ClassNode(); new ClassReader(bytes).accept(target, 0);
                    int slot = -1;
                    for (int i = 0; i < target.methods.size(); i++) {
                        MethodNode method = target.methods.get(i);
                        if (method.name.equals("trim") && method.desc.equals("(Z)Z")) { if (slot != -1) throw new IllegalStateException("Duplicate trim"); slot = i; }
                    }
                    if (slot < 0 || !target.superName.equals(donor.superName)) throw new IllegalStateException("Baseline ABI differs");
                    for (AbstractInsnNode instruction : replacement.instructions) {
                        if (instruction instanceof FieldInsnNode field && field.owner.equals(DONOR)) {
                            if (target.fields.stream().noneMatch(f -> f.name.equals(field.name) && f.desc.equals(field.desc))) throw new IllegalStateException("Missing field " + field.name);
                            field.owner = TARGET;
                        } else if (instruction instanceof MethodInsnNode call && call.owner.equals(DONOR)) {
                            boolean inheritedGetter = call.desc.equals("()I") && java.util.Set.of("getSectionCount", "getMinSectionPosition", "getMaxSectionPosition").contains(call.name);
                            if (!inheritedGetter && target.methods.stream().noneMatch(m -> m.name.equals(call.name) && m.desc.equals(call.desc))) throw new IllegalStateException("Missing method " + call.name);
                            call.owner = TARGET;
                        }
                        if (instruction instanceof FrameNode frame) {
                            if (frame.local != null) frame.local.replaceAll(value -> DONOR.equals(value) ? TARGET : value);
                            if (frame.stack != null) frame.stack.replaceAll(value -> DONOR.equals(value) ? TARGET : value);
                        }
                    }
                    target.methods.set(slot, replacement);
                    ClassWriter writer = new ClassWriter(0); target.accept(writer); bytes = writer.toByteArray(); patched++;
                } else { unchanged++; }
                ZipEntry copy = new ZipEntry(entry.getName()); copy.setTime(entry.getTime()); out.putNextEntry(copy); out.write(bytes); out.closeEntry();
            }
        }
        if (patched != 1) { Files.delete(output); throw new IllegalStateException("Expected exactly one patched adapter, got " + patched); }
        System.out.println("Patched trim(Z)Z only; preserved " + unchanged + " baseline entries. " + output);
    }
}
