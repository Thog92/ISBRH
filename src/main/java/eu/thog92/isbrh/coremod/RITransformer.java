package eu.thog92.isbrh.coremod;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import scala.tools.nsc.transform.SpecializeTypes;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RITransformer implements ITransformHandler {

    @Override
    public byte[] transform(String className, byte[] buffer) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(buffer);
        classReader.accept(classNode, 0);
        List<MethodNode> methods = classNode.methods;
        Iterator<MethodNode> iterator = methods.iterator();
        MethodNode renderItemMethod = null;
        String methodDesc = "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/resources/model/IBakedModel;Lnet/minecraft/client/renderer/block/model/ItemCameraTransforms$TransformType;)V";
        String transformType = "net/minecraft/client/renderer/block/model/ItemCameraTransforms$TransformType";
        String desc = "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemCameraTransforms$TransformType;)V";
        String itemStackClass = "net/minecraft/item/ItemStack";
        String itemBlockClass = "net/minecraft/item/ItemBlock";
        String getItemName = "getItem";
        String getItemDesc = "()Lnet/minecraft/item/Item;";
        String getBlockName = "getBlock";
        String blockClass = "net/minecraft/block/Block";
        String getRenderTypeName = "getRenderType";
        String getRenderTypeDesc = "()I";
        boolean ob;
        while (iterator.hasNext()) {
            MethodNode method = iterator.next();
            if ((method.name.equals("renderItem") && method.desc
                    .equals("(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/resources/model/IBakedModel;)V"))
                    || (method.name.equals("a") && method.desc
                    .equals("(Lamj;Lcxe;)V"))) {
                ob = method.name.equals("a");




                if (ob) {
                    transformType = "cmz";
                    desc = "(Lamj;Lcmz;)V";
                    itemStackClass = "amj";
                    itemBlockClass = "aju";
                    getItemName = "b";
                    getItemDesc = "()Lalq;";
                    getBlockName = "d";
                    getRenderTypeName = "b";
                    blockClass = "atr";
                    methodDesc = "(Lamj;Lcxe;Lcmz;)V";
                }
                String getBlockDesc = "()L" + blockClass + ";";
                InsnList toInject = new InsnList();


                for(int i = 0; i < method.instructions.size(); i++)
                {
                    AbstractInsnNode abstractInsnNode = method.instructions.get(i);
                    System.out.println(abstractInsnNode + ":" + Integer.toHexString(abstractInsnNode.getOpcode()));
                    if(abstractInsnNode instanceof MethodInsnNode)
                    {
                        MethodInsnNode targetMethod = ((MethodInsnNode)abstractInsnNode);
                        //System.out.println(targetMethod.name);
                        if ((targetMethod.name.equals("renderByItem") && targetMethod.desc
                                .equals("(Lnet/minecraft/item/ItemStack;)V"))
                                || (targetMethod.name.equals("a") && targetMethod.desc
                                .equals("(Lamj;)V"))) {
                            AbstractInsnNode targetGoto = targetMethod.getNext();
                            if(targetGoto instanceof  JumpInsnNode)
                            {
                                JumpInsnNode jumpInsnNode = ((JumpInsnNode) targetGoto);
                                LabelNode label = new LabelNode();

                                toInject.add(new VarInsnNode(Opcodes.ALOAD, 1));
                                toInject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, itemStackClass, getItemName, getItemDesc, false));
                                toInject.add(new TypeInsnNode(Opcodes.INSTANCEOF, itemBlockClass));
                                toInject.add(new JumpInsnNode(Opcodes.IFEQ, label));
                                toInject.add(new VarInsnNode(Opcodes.ALOAD, 1));
                                toInject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, itemStackClass, getItemName, getItemDesc, false));
                                toInject.add(new TypeInsnNode(Opcodes.CHECKCAST, itemBlockClass));
                                toInject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, itemBlockClass, getBlockName, getBlockDesc, false));
                                toInject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, blockClass, getRenderTypeName, getRenderTypeDesc, false));
                                toInject.add(new InsnNode(Opcodes.ICONST_4));
                                toInject.add(new JumpInsnNode(Opcodes.IF_ICMPLE, label));
                                toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                        "eu/thog92/isbrh/registry/RenderRegistry",
                                        "instance",
                                        "()Leu/thog92/isbrh/registry/RenderRegistry;",
                                        false));
                                toInject.add(new VarInsnNode(Opcodes.ALOAD, 1));
                                toInject.add(new FieldInsnNode(Opcodes.GETSTATIC,
                                        transformType, "NONE", "L" + transformType + ";"));
                                toInject.add(new MethodInsnNode(
                                        Opcodes.INVOKEVIRTUAL,
                                        "eu/thog92/isbrh/registry/RenderRegistry",
                                        "renderInventoryBlock", desc, false));
                                toInject.add(new JumpInsnNode(Opcodes.GOTO, jumpInsnNode.label));
                                toInject.add(label);
                                method.instructions.insert(jumpInsnNode.getNext(), toInject);
                                renderItemMethod = method;
                                System.out.println("PATCH");
                                break;
                            }


                        }

                    }
                }

            }  else if ((method.name.equals("shouldRenderItemIn3D") && method.desc
                    .equals("(Lnet/minecraft/item/ItemStack;)Z"))
                    || (method.name.equals("a") && method.desc
                    .equals("(Lamj;)Z"))) {
                ob = method.name.equals("a");
                String shouldRenderItemIn3DBodyDesc = "(Lnet/minecraft/client/renderer/entity/RenderItem;Lnet/minecraft/item/ItemStack;)Z";
                if (ob)
                    desc = "(Lcqh;Lamj;)Z";
                method.instructions.clear();
                method.localVariables = new ArrayList<LocalVariableNode>();
                method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                method.instructions.add(new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        "eu/thog92/isbrh/coremod/RenderAccessHook",
                        "shouldRenderItemIn3DBody", shouldRenderItemIn3DBodyDesc, false));
                method.instructions.add(new InsnNode(Opcodes.IRETURN));
            }

        }

        /*if(renderItemMethod != null)
        {
            MethodNode newMethod = new MethodNode(Opcodes.ACC_PUBLIC, renderItemMethod.name, methodDesc, null, renderItemMethod.exceptions.toArray(new String[renderItemMethod.exceptions.size()]));
            LabelNode label = new LabelNode();
            InsnList toInject = new InsnList();
            toInject.add(new VarInsnNode(Opcodes.ALOAD, 1));
            toInject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, itemStackClass, getItemName, getItemDesc, false));
            toInject.add(new TypeInsnNode(Opcodes.INSTANCEOF, itemBlockClass));
            toInject.add(new JumpInsnNode(Opcodes.IFEQ, label));
            toInject.add(new VarInsnNode(Opcodes.ALOAD, 1));
            toInject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, itemStackClass, getItemName, getItemDesc, false));
            toInject.add(new TypeInsnNode(Opcodes.CHECKCAST, itemBlockClass));
            toInject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, itemBlockClass, getBlockName, "()L" + blockClass + ";", false));
            toInject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, blockClass, getRenderTypeName, getRenderTypeDesc, false));
            toInject.add(new InsnNode(Opcodes.ICONST_4));
            toInject.add(new JumpInsnNode(Opcodes.IF_ICMPLE, label));
            toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    "eu/thog92/isbrh/registry/RenderRegistry",
                    "instance",
                    "()Leu/thog92/isbrh/registry/RenderRegistry;",
                    false));
            toInject.add(new VarInsnNode(Opcodes.ALOAD, 1));
            toInject.add(new FieldInsnNode(Opcodes.GETSTATIC,
                    transformType, "NONE", "L" + transformType + ";"));
            toInject.add(new MethodInsnNode(
                    Opcodes.INVOKEVIRTUAL,
                    "eu/thog92/isbrh/registry/RenderRegistry",
                    "renderInventoryBlock", desc, false));
            toInject.add(new JumpInsnNode(Opcodes.GOTO, label));
            toInject.add(label);
            newMethod.instructions.insert(toInject);
            classNode.methods.add(newMethod);
            System.out.println("Adding renderItem new method");
        }*/

        ClassWriter writer = new ClassWriter(0);
        classNode.accept(writer);
        byte[] patched = writer.toByteArray();
        try
        {
            FileOutputStream out = new FileOutputStream("RenderItem.class");
            out.write(patched);
            out.close();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }

        return patched;
    }

}
