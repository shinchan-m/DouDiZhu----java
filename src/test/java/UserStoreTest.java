import com.itheima.util.UserStore;

import java.nio.file.Files;
import java.nio.file.Path;

public class UserStoreTest {
    public static void main(String[] args) throws Exception {
        Path tempDir = Files.createTempDirectory("doudizhu-user-test");
        Path userFile = tempDir.resolve("users.txt");
        UserStore first = new UserStore(userFile);
        first.register("test_user_001", "123456");

        UserStore reloaded = new UserStore(userFile);
        if (!reloaded.authenticate("test_user_001", "123456")) {
            throw new AssertionError("重启后应能读取注册账号");
        }
        if (reloaded.authenticate("test_user_001", "wrong")) {
            throw new AssertionError("错误密码不能通过");
        }
        System.out.println("UserStoreTest passed");
    }
}
