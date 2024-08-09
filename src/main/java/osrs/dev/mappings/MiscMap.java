package osrs.dev.mappings;

import javassist.CtMethod;
import javassist.bytecode.MethodInfo;
import osrs.dev.annotations.mapping.Definition;
import osrs.dev.annotations.mapping.MappingSet;
import osrs.dev.modder.model.Mappings;

@MappingSet
public class MiscMap
{
    @Definition(targets = {"menuAction"})
    public static void findDoAction(CtMethod method)
    {
        if(Mappings.findByTag("menuAction") != null)
            return;

        MethodInfo info = method.getMethodInfo2();
        if(!info.getDescriptor().startsWith("(IIIIIILjava/lang/String;Ljava/lang/String;II") || !info.getDescriptor().endsWith(")V"))
            return;

        if(info.getCodeAttribute().getCodeLength() < 5000)
            return;

        Mappings.addMethod("menuAction", method.getName(), method.getDeclaringClass().getName(), info.getDescriptor());
    }
}
