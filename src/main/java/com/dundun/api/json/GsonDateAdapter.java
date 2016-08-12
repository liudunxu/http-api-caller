package com.dundun.api.json;

import java.lang.reflect.Type;
import java.util.Date;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import com.dundun.api.utils.GsonValueUtils;

/**
 * gson对date进行特殊处理
 * Created by dunxuliu on 2015/12/24.
 */
public class GsonDateAdapter implements JsonDeserializer<Date> {
    @Override
    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
        if (json != null && !json.isJsonNull()) {
            return GsonValueUtils.getAsDate(json.getAsString());
        }
        return null;
    }
}
