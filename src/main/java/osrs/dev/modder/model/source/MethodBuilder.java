package osrs.dev.modder.model.source;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import lombok.SneakyThrows;

public class MethodBuilder {
    private String _scope = "";
    private String _modifier = "";
    private String _final = "";
    private String _return = "";
    private String _arrayReturn = " ";
    private String _name = "";
    private String _args = "";
    private String _body = "";

    //Scopes
    public MethodBuilder Public()
    {
        _scope = "public ";
        return this;
    }

    public MethodBuilder Protected()
    {
        _scope = "protected ";
        return this;
    }

    public MethodBuilder Private()
    {
        _scope = "private ";
        return this;
    }

    public MethodBuilder noScope()
    {
        _scope = "";
        return this;
    }

    //Modifiers
    public MethodBuilder Static()
    {
        _modifier = "static ";
        return this;
    }

    public MethodBuilder Abstract()
    {
        _modifier = "abstract ";
        return this;
    }

    public MethodBuilder noModifier()
    {
        _modifier = "";
        return this;
    }

    public MethodBuilder Final()
    {
        _final = "final ";
        return this;
    }

    public MethodBuilder notFinal()
    {
        _final = "";
        return this;
    }

    //return types
    public MethodBuilder Void()
    {
        _return = "void";
        return this;
    }

    public MethodBuilder String()
    {
        _return = "String";
        return this;
    }

    public MethodBuilder Int()
    {
        _return = "int";
        return this;
    }

    public MethodBuilder Long()
    {
        _return = "long";
        return this;
    }

    public MethodBuilder Short()
    {
        _return = "short";
        return this;
    }

    public MethodBuilder Byte()
    {
        _return = "byte";
        return this;
    }

    public MethodBuilder Returns(String type)
    {
        _return = type;
        return this;
    }

    public MethodBuilder Array()
    {
        _arrayReturn = "[] ";
        return this;
    }

    //method name
    public MethodBuilder withName(String name)
    {
        _name = name;
        return this;
    }

    //Arguments
    public MethodBuilder withArgs(String args)
    {
        _args = args;
        return this;
    }

    //body
    public MethodBuilder withBody(String body)
    {
        _modifier = _modifier.replace("abstract ", "");
        if(!body.startsWith("{"))
            body = "{" + body;
        if(!body.endsWith("}"))
            body = body + "}";
        _body = body;
        return this;
    }

    /**
     * sets all except for the name and body
     * @param method method to mimic properties from
     * @return this
     */
    public MethodBuilder generateFromTemplate(CtMethod method)
    {
        int mod = method.getModifiers();
        _scope = Modifier.isPrivate(mod) ? "private " : (Modifier.isProtected(mod) ? "protected " : (Modifier.isPublic(mod) ? "public " : ""));
        _modifier = Modifier.isStatic(mod) ? "static " : (Modifier.isAbstract(mod) ? "abstract " : "");
        _final = Modifier.isFinal(mod) ? "final " : "";
        try
        {
            _return = method.getReturnType().getName() + " ";
        }
        catch (Exception ex)
        {
            _return = ex.getMessage() + " ";
        }
        return this;
    }

    /**
     * returns the build method
     * @return method src
     */
    public String get()
    {
        if(_body.equals("") && !_modifier.equals("abstract "))
        {
            return _scope + _modifier + _final + _return + _arrayReturn + _name + "(" + _args + ")" + (_return.equals("void ") ? "{}" : "{ return null; }");
        }
        return _scope + _modifier + _final + _return + _arrayReturn + _name + "(" + _args + ")" + _body;
    }

    /**
     * returns the build method
     * @return method src
     */
    @SneakyThrows
    public CtMethod make(CtClass declaring)
    {
        String body = _body;
        if(_return.equals("void"))
            _body = "{}";
        else
            _body = "{ return null; }";
        String src = get();
        CtMethod method = CtNewMethod.make(src, declaring);
        method.setBody(body);
        return method;
    }
}