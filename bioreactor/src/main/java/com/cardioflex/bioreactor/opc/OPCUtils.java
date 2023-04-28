package com.cardioflex.bioreactor.opc;

import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;

public class OPCUtils {
    public static Object toJsonValue(DataValue dataValue) {
        if (dataValue == null) return null;
        Variant variant = dataValue.getValue();
        if (variant == null || variant.isNull()) return null;
        Object val = variant.getValue();
        if (val instanceof Double) {
            if (Double.isNaN((Double) val)) return null;
            return val;
        }
        if (val instanceof Float) {
            if (Float.isNaN((Float) val)) return null;
            return val;
        }
        return val;
    }
}
