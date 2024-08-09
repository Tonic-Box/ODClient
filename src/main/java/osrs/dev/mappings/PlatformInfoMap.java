package osrs.dev.mappings;

import javassist.CtMethod;
import osrs.dev.annotations.mapping.Definition;
import osrs.dev.annotations.mapping.MappingSet;
import osrs.dev.modder.model.Mappings;
import osrs.dev.modder.model.javassist.CodeBlock;
import osrs.dev.modder.model.javassist.MethodDefinition;
import osrs.dev.modder.model.javassist.enums.BlockType;

@MappingSet
public class PlatformInfoMap
{
    @Definition(targets = {"getDeviceId", "PlatformInfo"})
    public static void findGetDeviceId(CtMethod method)
    {
        if(Mappings.findByTag("getDeviceId") != null)
            return;

        try
        {
            MethodDefinition methodDefinition = new MethodDefinition(method);

            for(CodeBlock block : methodDefinition.getBody())
            {
                if(!block.getBlockType().equals(BlockType.LOCAL_STORE))
                    continue;

                if(!block.containsValue("12345678-0000-0000-0000-123456789012"))
                    continue;

                Mappings.addMethod("getDeviceId", method.getName(), method.getDeclaringClass().getName(), method.getMethodInfo2().getDescriptor());
                Mappings.addClass("PlatformInfo", method.getDeclaringClass().getName());
                return;
            }
        }
        catch (Exception ignored)
        {
        }
    }
}
