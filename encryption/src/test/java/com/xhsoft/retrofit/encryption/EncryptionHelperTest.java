package com.xhsoft.retrofit.encryption;

import com.xhsoft.retrofit.encryption.tools.EncryptionHelper;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 测试加密解密功能
 *
 * Created by zhangxh on 16/4/16.
 */
public class EncryptionHelperTest {

    @Test
    public void testEncryption() throws Exception {
        String key = "9E7E598F42CB0FE3314830FABC8968671877DB781615C104";

        String message = "hello world 你好世界!";
        String a = EncryptionHelper.encryptText(message, key, new byte[]{});
        String b = EncryptionHelper.dencryptText(a, key, new byte[]{});

        assertThat(message).isEqualTo(b);
    }

    @Test
    public void testEncryptionBytes() throws Exception {
        String key = "9E7E598F42CB0FE3314830FABC8968671877DB781615C104";

        String message = "hello world 你好世界!";
        byte[] a = EncryptionHelper.encryptBytes(message.getBytes("gbk"), key, new byte[]{});
        byte[] b = EncryptionHelper.dencryptBytes(a, key, new byte[]{});

        assertThat(message).isEqualTo(new String(b, "gbk"));
    }
}
