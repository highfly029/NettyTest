/**
 * @ClassName ClientStart
 * @Description TODO
 * @Author liyunpeng
 * @Date 2018/11/15 14:59
 **/
public class ClientStart {
    public static void main(String[] args) {
        int clientNum = 0;
        int maxNum = ConfigPropUtil.getIntValue("CLIENT_NUM");
        String host = ConfigPropUtil.getValue("HOST");
        int port = ConfigPropUtil.getIntValue("PORT");
        try {
            while (true) {
                Thread.sleep(200);
                if (clientNum >= maxNum) {
                    System.out.println("allTime="+ClientHandler.allDelta.get()+" allReceivedNum="+ClientHandler.receivedNum.get());
                    Thread.sleep(60 * 1000);
                    continue;
                }
                Client client = new Client();
                client.connect(host, port);
                clientNum++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
