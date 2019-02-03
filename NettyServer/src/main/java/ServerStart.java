/**
 * @ClassName ServerStart
 * @Description TODO
 * @Author liyunpeng
 * @Date 2018/11/15 14:27
 **/
public class ServerStart {
    public static void main(String[] args) {
        try {
            Server server = new Server();
            server.run(12345);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
