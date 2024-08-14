package osrs.dev.mappings;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.Opcode;
import osrs.dev.annotations.mapping.Definition;
import osrs.dev.annotations.mapping.MappingSet;
import osrs.dev.modder.model.Mappings;
import osrs.dev.modder.model.javassist.CodeBlock;
import osrs.dev.modder.model.javassist.MethodDefinition;
import osrs.dev.modder.model.javassist.enums.BlockType;
import osrs.dev.modder.model.javassist.instructions.FieldLine;
import osrs.dev.modder.model.javassist.instructions.MethodLine;
import osrs.dev.modder.model.javassist.instructions.ValueLine;

@MappingSet
public class LoginMap
{
    @Definition(targets = {"Login","Login_username","Login_password","updateGameState"})
    public static void mapLoginUsername(CtMethod method)
    {
        try
        {
            if(Mappings.findByTag("Login_username") != null)
                return;

            MethodDefinition definition = new MethodDefinition(method);
            if(!definition.containsBlockWithValue("Please enter your username/email address."))
                return;

            Mappings.addClass("Login", method.getDeclaringClass().getName());

            MethodLine updateGameState;
            FieldLine fieldLine;
            String username = null;
            String password = null;
            for(CodeBlock block : definition.getBody())
            {
                if(block.hasMethodCall("java.lang.String", "trim", "()Ljava/lang/String;") && username == null)
                {
                    fieldLine = block.findFirst(m -> m.getOpcode() == Opcode.GETSTATIC);
                    if(fieldLine != null)
                    {
                        Mappings.addField("Login_username", fieldLine.getName(), fieldLine.getClazz(), fieldLine.getType());
                        username = fieldLine.getName();
                    }
                }
                else if(block.hasMethodCall("java.lang.String", "length", "()I") && username != null && password == null)
                {
                    String finalUsername = username;
                    FieldLine fLine = block.findFirst(m -> {
                        if(m.getOpcode() != Opcode.GETSTATIC)
                            return false;

                        FieldLine line = m.transpose();
                        return !line.getName().equals(finalUsername);
                    });
                    if(fLine != null)
                    {
                        password = fLine.getName();
                        Mappings.addField("Login_password", fLine.getName(), fLine.getClazz(), fLine.getType());
                    }
                }
                else if(block.findFirst(i -> {
                    if(!(i instanceof ValueLine))
                        return false;

                    ValueLine line = i.transpose();
                    if(!(line.getValue() instanceof Integer))
                        return false;

                    return ((int)line.getValue()) == 20;
                }) != null)
                {
                    updateGameState = block.findFirst(m -> m instanceof MethodLine);
                    if(updateGameState == null)
                        continue;

                    Mappings.addMethod("updateGameState", updateGameState.getName(), updateGameState.getClazz(), updateGameState.getType());
                }
            }
        }
        catch (Exception ex)
        {
        }
    }

    @Definition(targets = {"forceDisconnect","logOut","serverCycle"})
    public static void findLogoutStuff(CtMethod method)
    {
        try {
            MethodDefinition definition = new MethodDefinition(method);
            boolean pass = false;
            for (CodeBlock block : definition.getBody()) {
                if (block.containsValue("The game servers are currently being updated.")) {
                    pass = true;
                    break;
                }
            }
            if (!pass)
                return;

            Mappings.addMethod("forceDisconnect", method.getName(), method.getDeclaringClass().getName(), method.getMethodInfo2().getDescriptor());

            CodeBlock block = definition.getBody().get(0);
            if (!block.getBlockType().equals(BlockType.VOID_METHOD_CALL))
                return;

            MethodLine line = block.findFirst(m -> m.getOpcode() == Opcode.INVOKESTATIC);
            CtClass clazz = Mappings.getClazz(line.getClazz());
            CtMethod logOut = clazz.getMethod(line.getName(), line.getType());

            Mappings.addMethod("logOut", logOut.getName(), logOut.getDeclaringClass().getName(), logOut.getMethodInfo2().getDescriptor());

            MethodDefinition logOutDef = new MethodDefinition(logOut);
            for (CodeBlock codeBlock : logOutDef.getBody())
            {
                if(codeBlock.getBlockType().equals(BlockType.FIELD_STORE))
                {
                    FieldLine fieldLine = codeBlock.findFirst(m -> m.hasOpcode(Opcode.PUTSTATIC));
                    if(fieldLine != null && fieldLine.getType().equals("I"))
                    {
                        Mappings.addField("serverCycle", fieldLine.getName(), fieldLine.getClazz(), fieldLine.getType());
                        break;
                    }
                }
            }
        }
        catch (Exception ignored)
        {
        }
    }
}
