package osrs.dev.modder.model;

import lombok.Getter;

import java.math.BigInteger;

@Getter
public class Garbage {
    private final Number getterValue;
    private final Number setterValue;
    public Garbage(Number garbage, boolean isSetter)
    {
        if(isSetter)
        {
            this.setterValue = garbage;
            if (this.setterValue.longValue() != 1)
                this.getterValue = modInverse(garbage);
            else
                this.getterValue = setterValue;
        }
        else
        {
            this.getterValue = garbage;
            if (this.getterValue.longValue() != 1)
                this.setterValue = modInverse(garbage);
            else
                this.setterValue = getterValue;
        }
    }

    public Number modInverse(Number val)
    {
        boolean instanceOfLong = val instanceof Long;
        BigInteger bigint = modInverse(BigInteger.valueOf(instanceOfLong ? val.longValue() : val.intValue()), instanceOfLong ? 64 : 32);
        return instanceOfLong ? bigint.longValue() : bigint.intValue();
    }

    public BigInteger modInverse(BigInteger val, int bits)
    {
        BigInteger shift = BigInteger.ONE.shiftLeft(bits);
        return val.modInverse(shift);
    }

    public static String getGarbageSetter(Mapping entry)
    {
        String garbVal = "";
        if (entry.getGarbage() != null)
        {
            String cast = entry.getDataType();
            if (cast.equals("int"))
            {
                garbVal = " * " + "osrs.dev.modder.model.Mappings.getFieldSetter(\"" + entry.getObfuscatedClass() + "\", \"" + entry.getObfuscatedName() + "\").intValue();";
            } else
            {
                garbVal = " * " + "osrs.dev.modder.model.Mappings.getFieldSetter(\"" + entry.getObfuscatedClass() + "\", \"" + entry.getObfuscatedName() + "\").longValue();";
            }
        }

        return garbVal;
    }

    public static String getGarbageGetter(Mapping entry)
    {
        String garbVal = "";
        if (entry.getGarbage() != null)
        {
            String cast = entry.getDataType();
            if (cast.equals("int"))
            {
                garbVal = " * " + "osrs.dev.modder.model.Mappings.getFieldGetter(\"" + entry.getObfuscatedClass() + "\", \"" + entry.getObfuscatedName() + "\").intValue()";
            } else
            {
                garbVal = " * " + "osrs.dev.modder.model.Mappings.getFieldGetter(\"" + entry.getObfuscatedClass() + "\", \"" + entry.getObfuscatedName() + "\").longValue()";
            }
        }

        return garbVal;
    }
}