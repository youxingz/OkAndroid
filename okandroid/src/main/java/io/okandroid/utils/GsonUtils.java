package io.okandroid.utils;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public class GsonUtils {
    private static Gson gson;

    public static Gson getInstance() {
        if (gson == null) {
            gson = new GsonBuilder()
                    // 设置自定义解析（不支持协变）
                    // .registerTypeAdapter(Id.class, new IdTypeAdapter())
                    // 设置自定义解析（支持协变）
                    // .registerTypeHierarchyAdapter(List.class, new MyListTypeAdapter())
                    // 设置自定义解析（以工厂方式）
                    // .registerTypeAdapterFactory(new IdTypeAdapterFactory())
                    // 设置日期格式
                    .setDateFormat("yyyy-MM-dd HH:mm:ss:SSS")
                    // 设置自动切换命名风格规则（默认不切换命名风格）
                    .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                    // 设置过滤指定字段标识符（默认只过滤 transient 和 static 字段）
                    .excludeFieldsWithModifiers(Modifier.TRANSIENT | Modifier.STATIC)
                    // 设置类或字段过滤规则
                    // .setExclusionStrategies(new MyExclusionStrategy1())
                    // 设置过滤规则（只适用于序列化）
                    // .addSerializationExclusionStrategy(new MyExclusionStrategy2())
                    // 设置过滤规则（只适用于反序列化）
                    // .addDeserializationExclusionStrategy(new MyExclusionStrategy3())
                    // 设置序列化版本号
                    .setVersion(1.0)
                    // 启用非基础类型 Map Key
                    .enableComplexMapKeySerialization()
                    // 启用不过滤空值（默认会过滤空值）
                    .serializeNulls()
                    // 启用 Json 格式化
                    .setPrettyPrinting().create();
        }
        return gson;
    }

    public static <T> List<T> fromJsonList(String json, Class clazz) {
        Type type = new ParameterizedTypeImpl(clazz);
        List<T> list = new Gson().fromJson(json, type);
        return list;
    }

    private static class ParameterizedTypeImpl implements ParameterizedType {
        Class clazz;

        public ParameterizedTypeImpl(Class clz) {
            clazz = clz;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return new Type[]{clazz};
        }

        @Override
        public Type getRawType() {
            return List.class;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    }
}
