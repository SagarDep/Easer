/*
 * Copyright (c) 2016 - 2017 Rui Zhao <renyuneyun@gmail.com>
 *
 * This file is part of Easer.
 *
 * Easer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Easer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Easer.  If not, see <http://www.gnu.org/licenses/>.
 */

package ryey.easer.plugins.event.nfc_tag;

import android.os.Parcel;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;

import ryey.easer.commons.C;
import ryey.easer.commons.IllegalStorageDataException;
import ryey.easer.commons.plugindef.eventplugin.EventType;
import ryey.easer.plugins.event.TypedEventData;


public class NfcTagEventData extends TypedEventData {

    private static final String K_ID = "id";

    private byte[] id;

    {
        default_type = EventType.is;
        availableTypes = EnumSet.of(EventType.is, EventType.is_not);
    }

    static String byteArray2hexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = Character.forDigit(v >>> 4, 16);
            hexChars[j * 2 + 1] = Character.forDigit(v & 0x0F, 16);
        }
        return new String(hexChars);
    }

    static byte[] hexString2byteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public NfcTagEventData() {}

    public NfcTagEventData(String id_str) {
        set(id_str);
    }

    @NonNull
    @Override
    public Object get() {
        return id;
    }

    @Override
    public void set(@NonNull Object obj) {
        if (obj instanceof String) {
            set(hexString2byteArray((String) obj));
        } else if (obj instanceof byte[]) {
            id = (byte[]) obj;
        } else {
            throw new RuntimeException("illegal data");
        }
    }

    @Override
    public String toString() {
        return byteArray2hexString(id);
    }

    @Override
    public boolean isValid() {
        if (id == null)
            return false;
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof NfcTagEventData))
            return false;
        return Arrays.equals(id, ((NfcTagEventData) obj).id);
    }

    @Override
    public void parse(XmlPullParser parser, int version) throws IOException, XmlPullParserException, IllegalStorageDataException {
        throw new IllegalAccessError();
    }

    @Override
    public void serialize(XmlSerializer serializer) throws IOException {
        throw new IllegalAccessError();
    }

    @Override
    public void parse(@NonNull String data, @NonNull C.Format format, int version) throws IllegalStorageDataException {
        switch (format) {
            default:
                try {
                    JSONObject jsonObject = new JSONObject(data);
                    String tag_id_str = jsonObject.getString(K_ID);
                    id = hexString2byteArray(tag_id_str);
                } catch (JSONException e) {
                    e.printStackTrace();
                    throw new IllegalStorageDataException(e.getMessage());
                }
        }
    }

    @NonNull
    @Override
    public String serialize(@NonNull C.Format format) {
        String res;
        switch (format) {
            default:
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(K_ID, byteArray2hexString(id));
                } catch (JSONException e) {
                    e.printStackTrace();
                    throw new IllegalStateException();
                }
                res = jsonObject.toString();
        }
        return res;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id.length);
        dest.writeByteArray(id);
    }

    public static final Creator<NfcTagEventData> CREATOR
            = new Creator<NfcTagEventData>() {
        public NfcTagEventData createFromParcel(Parcel in) {
            return new NfcTagEventData(in);
        }

        public NfcTagEventData[] newArray(int size) {
            return new NfcTagEventData[size];
        }
    };

    private NfcTagEventData(Parcel in) {
        id = new byte[in.readInt()];
        in.readByteArray(id);
    }
}