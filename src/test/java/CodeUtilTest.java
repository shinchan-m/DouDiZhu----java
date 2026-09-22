import com.itheima.util.CodeUtil;

public class CodeUtilTest {
    public static void main(String[] args) {
        for (int i = 0; i < 1000; i++) {
            String code = CodeUtil.getCode();
            if (code.length() != 5) {
                throw new AssertionError("验证码长度错误: " + code);
            }
            int digits = 0;
            for (char c : code.toCharArray()) {
                if (Character.isDigit(c)) {
                    digits++;
                } else if (!Character.isLetter(c)) {
                    throw new AssertionError("验证码包含非法字符: " + code);
                }
            }
            if (digits != 1) {
                throw new AssertionError("验证码必须包含且只包含一个数字: " + code);
            }
        }
        System.out.println("CodeUtilTest passed");
    }
}
