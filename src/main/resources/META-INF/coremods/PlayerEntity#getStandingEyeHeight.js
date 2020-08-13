var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');

var mappedMethodName = ASMAPI.mapMethod('func_213348_b');

function initializeCoreMod() {
    return {
        'coremodmethod': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.entity.player.PlayerEntity',
                'methodName': mappedMethodName,
                'methodDesc': '(Lnet/minecraft/entity/Pose;Lnet/minecraft/entity/EntitySize;)F'
            },
            'transformer': function(method) {
                var list = ASMAPI.listOf();
                var instruction = ASMAPI.findFirstInstruction(method, Opcodes.GETSTATIC);
                var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');
                list.add(new FieldInsnNode(Opcodes.GETSTATIC, 'com/github/kyazuki/growthmod/GrowthModConfig', 'change_eyeheight', 'Z'));
                var LabelNode = Java.type('org.objectweb.asm.tree.LabelNode');
                var label = new LabelNode();
                var JumpInsnNode = Java.type('org.objectweb.asm.tree.JumpInsnNode');
                list.add(new JumpInsnNode(Opcodes.IFEQ, label));
                var label2 = new LabelNode();
                list.add(label2);
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                list.add(new VarInsnNode(Opcodes.ALOAD, 1));
                var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
                list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'com/github/kyazuki/growthmod/events/CommonEventHandler', 'getScaledStandingEyeHeight', '(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/Pose;)F'));
                var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
                list.add(new InsnNode(Opcodes.FRETURN));
                list.add(label);
                method.instructions.insertBefore(instruction, list);
                return method;
            }
        }
    }
}