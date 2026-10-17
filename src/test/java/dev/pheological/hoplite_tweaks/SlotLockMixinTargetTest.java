package dev.pheological.hoplite_tweaks;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import static org.junit.jupiter.api.Assertions.*;

/** Catches injection ambiguity that Java compilation alone cannot detect. */
class SlotLockMixinTargetTest {
    @Test
    void inventoryInjectionsResolveToOneCompatibleMethod() throws IOException {
        verify("SlotLockScreenMixin", "net/minecraft/client/gui/screens/inventory/AbstractContainerScreen");
        verify("SlotLockCreativeMixin", "net/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen");
    }

    private void verify(String mixin, String targetName) throws IOException {
        ClassNode target = read(targetName);
        ClassNode source = read("dev/pheological/hoplite_tweaks/mixin/" + mixin);
        for (MethodNode handler : source.methods) {
            if (handler.visibleAnnotations == null) continue;
            for (AnnotationNode annotation : handler.visibleAnnotations) {
                if (!annotation.desc.equals("Lorg/spongepowered/asm/mixin/injection/Inject;")) continue;
                int index = annotation.values.indexOf("method");
                assertTrue(index >= 0);
                for (Object value : (List<?>) annotation.values.get(index + 1)) {
                    String selector = (String) value;
                    List<MethodNode> matches = target.methods.stream().filter(method ->
                        selector.equals(method.name) || selector.equals(method.name + method.desc)).toList();
                    assertEquals(1, matches.size(), mixin + ": ambiguous or missing target " + selector);
                    Type[] actual = Type.getArgumentTypes(handler.desc);
                    Type[] expected = Type.getArgumentTypes(matches.getFirst().desc);
                    if (actual.length == 1) continue; // Callback-only handlers intentionally omit target arguments.
                    assertEquals(expected.length + 1, actual.length, handler.name);
                    for (int i = 0; i < expected.length; i++) assertEquals(expected[i], actual[i], handler.name);
                }
            }
        }
    }

    private ClassNode read(String name) throws IOException {
        try (var stream = getClass().getClassLoader().getResourceAsStream(name + ".class")) {
            assertNotNull(stream, name);
            ClassNode node = new ClassNode();
            new ClassReader(stream).accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            return node;
        }
    }
}
