package kkboxreg3;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class KKBOXreg3 {
    static DefaultHttpClient httpclient = new DefaultHttpClient();
    static FileWriter outFile;
    static PrintWriter out;
    static String uid =new String();
    static String sid_token=new String();
    static String BIGipServer=new String();
    static String phpsessid=new String();
    static String kkboxcheck=new String();
    static String piccookie=new String();
    static JLabel idlabel = new JLabel("KKBOX Reg v1.2");
    static JTextArea textarea = new JTextArea();
    static String domain="@sharklasers.com";
    static JPopupMenu pop = new JPopupMenu();
    static JPopupMenu picpop = new JPopupMenu();
    static int conY=0;
    static JPanel panel = new JPanel();
    static void GetUid() throws IOException{
        HttpGet httpget = new HttpGet("http://api.guerrillamail.com/ajax.php?f=get_email_address");
       ResponseHandler<String> responseHandler = new BasicResponseHandler();
       String responseBody = httpclient.execute(httpget, responseHandler);
       out.println(responseBody);
       String addr = getvalue(responseBody,"email_addr",1);
       String u="";
       for(int i=0;i<addr.length();i++){
           char c = addr.charAt(i);
           if (c=='@'){
               break;
           }
           else{
               u=u+addr.charAt(i);
           }
       }
       uid=u;
       sid_token = getvalue(responseBody,"sid_token",1);
    }
    static String getvalue (String text,String fn,int n){
        text = text.replaceAll("\"", "");
        int num = text.indexOf(fn) + fn.length() + n;
        String v = "";
        for(int i=num;i<text.length();i++){
            char c = text.charAt(i);
            if(c=='"' || c==',' || c=='}'){
                break;
            }
            else {
                v= v + text.charAt(i);
            }
        }
        return v;
    }
    static void stmail() throws IOException{
        try{
        HttpGet countmail = new HttpGet("http://api.guerrillamail.com/ajax.php?f=check_email&sid_token="
               + sid_token + "&seq=1");
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String responseBody = httpclient.execute(countmail, responseHandler);
        }
        finally{out.println("stmail DONE");}
    }
    static void GetRegSite() throws IOException{
        HttpGet httpget = new HttpGet("https://ssl.kkbox.com.tw/mobile_reg/reg.php");
        httpget.addHeader("User-Agent", "Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; "+
                "LG-L160L Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) "+
                "Version/4.0 Mobile Safari/534.30");
        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();
        out.println(response);
        String resp = response.toString();
        int hk = resp.lastIndexOf("本服務目前提供使用者於臺灣地區登入");
        if(hk==-1){
            out.println("TW IP");
        }
        else{
            out.println("NOT Taiwan IP.Can not register KKBOX in Taiwan.");
            idlabel.setText("NOT Taiwan IP.");
            System.exit(0);
        }
        out.println("Login form get: " + response.getStatusLine());
        EntityUtils.consume(entity);
        out.println("Initial set of cookies:");
        List<Cookie> cookies = httpclient.getCookieStore().getCookies();
        if (cookies.isEmpty()) {
            out.println("None");
        } else {
            for (int i = 0; i < cookies.size(); i++) {
                out.println("- " + cookies.get(i).toString());
                String cname = cookies.get(i).getName();
                switch (cname) {
                    case "BIGipServerBS1s_SSL":
                        BIGipServer = cookies.get(i).getValue();
                        break;
                    case "PHPSESSID":
                        phpsessid = cookies.get(i).getValue();
                        break;
                    case "kkbox_reg_check_cookie":
                        kkboxcheck = cookies.get(i).getValue();
                        break;
                }
            }
            piccookie=phpsessid+"; "+BIGipServer;
        }
    }
    static void RegPic() throws IOException{
        try{
            HttpGet picget = new HttpGet("https://ssl.kkbox.com.tw/reg_pic_auth.php");
            picget.addHeader("User-Agent", "Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
            picget.addHeader("Cookie",piccookie);
            HttpResponse respon = httpclient.execute(picget);
            HttpEntity entityt = respon.getEntity();
            File storeFile = new File("pic.png");
            try (FileOutputStream output = new FileOutputStream(storeFile)) {
                entityt.writeTo(output);
            }
        }finally{
            out.println("Reg picture downloaded!");
        }
    }
    static void formpost(String piccode,String email) throws IOException{
        HttpPost post = new HttpPost("https://ssl.kkbox.com.tw/mobile_reg/reg.php");
        post.addHeader("User-Agent", "Mozilla/5.0 (Linux; U; Android 4.0.3; "+
                "ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30"+
                " (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        post.addHeader("Cookie",kkboxcheck+"; "+piccookie);
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("uid", email));
        nvps.add(new BasicNameValuePair("nick", uid));
        nvps.add(new BasicNameValuePair("password1", "7969"));
        nvps.add(new BasicNameValuePair("password2", "7969"));
        nvps.add(new BasicNameValuePair("authword", piccode));//piccode
        nvps.add(new BasicNameValuePair("coupon", ""));//none
        nvps.add(new BasicNameValuePair("submit_mail", "透過 email 認證"));
        post.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String responseBody = httpclient.execute(post, responseHandler);
        out.println(responseBody);
        int codewrong = responseBody.lastIndexOf("驗證碼錯誤");
        if (codewrong==-1){
            out.println("CodeWrong==NULL");
            conY=1;
        }
        else{
            conY=0;
            out.println("CodeWrong");
            idlabel.setText("Reg Code Wrong");
        }
    }
    static void checkcode() throws InterruptedException, IOException{
        int co = 0;
        String count = "";
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        while (co == 0) {
            if (conY==0){
                break;
            }
            Thread.sleep(10000);
            idlabel.setText("checking email");
            HttpGet countmail2 = new HttpGet("http://api.guerrillamail.com/ajax.php?f=check_email&sid_token="
                    + sid_token + "&seq=1");
            String responseBody = httpclient.execute(countmail2, responseHandler);
            out.println(responseBody);
            count = getvalue(responseBody, "count", 1);
            out.println("count:" + count);
            co = Integer.parseInt(count);
            count = "";
            out.println("co:" + co);
            if (co != 0) {
                out.println(responseBody);
                String id = getvalue(responseBody, "mail_id", 1);
                out.println(id);
                idlabel.setText("Finding KeyURL..");
                HttpGet fetchmail = new HttpGet("http://api.guerrillamail.com/ajax.php?f=fetch_email&sid_token="
                        + sid_token + "&email_id=" + id);
                responseBody = httpclient.execute(fetchmail, responseHandler);
                out.println(responseBody);
                check(responseBody);
                out.println("=====DONE======");
                break;
            }
        }
    }
    static void check(String html) throws IOException{
        int l = html.length();
        int k = html.lastIndexOf("reg_confirm.php?key=");
        String keyurl = "";
        if (k == -1) {
            out.println("index NONE");
        } else {
            keyurl = "";
            for (int j = k; j < l; j++) {
                if (html.charAt(j) == '"' || html.charAt(j) == '\\') {
                    out.println(j);
                    for (int i = k; i < j; i++) {
                        keyurl = keyurl + html.charAt(i);
                    }
                    break;
                }
            }
            keyurl = "http://member.kkbox.com.tw/" + keyurl;
            out.println("keyurl:" + keyurl);
            idlabel.setText("Posting Key..");
            HttpGet httpkey = new HttpGet(keyurl);
            httpkey.addHeader("User-Agent", "Mozilla/5.0 (Linux; U; Android 4.0.3;"+
                    " ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30 (KHTML, "+
                    "like Gecko) Version/4.0 Mobile Safari/534.30");
            ResponseHandler<String> responseHandlers = new BasicResponseHandler();
            String responseBodys = httpclient.execute(httpkey, responseHandlers);
            out.println(responseBodys);
            out.println("Key post DONE!");
            idlabel.setText("Register Done.");
            textarea.setText(uid + domain);
        }
    }
    
    public static void main(String[] args) throws IOException {
        outFile = new FileWriter("log.txt");
        out = new PrintWriter(outFile);
        GetUid();
        stmail();
        GetRegSite();
        RegPic();
        //JFrame
        JFrame frm = new JFrame("KKBOX Reg v1.2");
        frm.setSize(400,200);
        frm.setResizable(false);
        frm.getContentPane().setLayout(null);
        frm.setLocation(100,100);
        frm.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                out.close();
                System.exit(0);
            }
        });
        JPanel Pan=new JPanel(null);
        Pan.setLocation(0,0);
        Pan.setSize(390,35);
        idlabel.setBounds(10, 5, 100, 30);
        textarea.setLocation(170, 40);
        textarea.setSize(215, 60);
        panel.setLocation(10, 40);
        panel.setSize(155,60);
        panel.add(reLoadPic());
        JButton button = new JButton("Register");
        button.setLocation(210, 110);
        button.setSize(175, 50);
        button.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                if ("".equals(textarea.getText())) {
                    idlabel.setText("Plaese enter reg code");
                } else {
                    try {
                        idlabel.setText("Posting form..");
                        String email = uid + domain;
                        formpost(textarea.getText(), email);
                        checkthread c1 = new checkthread();
                        c1.setName("C1");
                        c1.start();                        
                    } catch (IOException ex) {
                        Logger.getLogger(KKBOXreg3.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        JButton bt2 = new JButton("Power by 海賊電腦工作室");
        bt2.setLocation(10, 110);
        bt2.setSize(190, 50);
        bt2.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                String url = "http://my-one-piece.blogspot.com/";
                try {
                    java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
                } catch (IOException ex) {
                    Logger.getLogger(KKBOXreg3.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        String[] choices = {"@sharklasers.com", "@guerrillamailblock.com",
            "@guerrillamail.com", "@guerrillamail.net", "@guerrillamail.biz",
            "@guerrillamail.de", "@guerrillamail.org", "@spam4.me"};
        JComboBox box = new JComboBox(choices);
        box.setBounds(115, 5, 270, 30);
        box.addItemListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED)
                {
                    idlabel.setText(e.getItem().toString());
                    domain=e.getItem().toString();
                }
            }
        });
        Pan.add(idlabel);
        Pan.add(box);
        JMenuItem pmi = new JMenuItem("Refresh");
        pmi.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    RegPic();
                    panel.removeAll();
                    panel.repaint();
                    panel.setLocation(10, 40);
                    panel.setSize(155,60);
                    panel.add(reLoadPic());
                    panel.validate();
                } catch (IOException ex) {
                    Logger.getLogger(KKBOXreg3.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        picpop.add(pmi);
        panel.addMouseListener(new java.awt.event.MouseAdapter(){
            public void mousePressed(java.awt.event.MouseEvent evt){
                PicPressed(evt);
            }
        });
        JMenuItem mi = new JMenuItem("Copy");
        mi.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection contents = new StringSelection(textarea.getText());
                clipboard.setContents(contents, null);
            }
        });
        pop.add(mi);
        textarea.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt){
                FMousePressed(evt);
            }
        });
        frm.getContentPane().add(button);
        frm.getContentPane().add(bt2);
        frm.getContentPane().add(textarea);
        frm.getContentPane().add(Pan);
        frm.getContentPane().add(panel);
        frm.setVisible(true);
        //JFrame
        
        //out.close();
    }
    static void FMousePressed(MouseEvent evt){
        int mods=evt.getModifiers();
        if((mods&InputEvent.BUTTON3_MASK)!=0){
            pop.show(textarea, evt.getX(), evt.getY());
        }
    }
    static void PicPressed(MouseEvent evt){
        int mods=evt.getModifiers();
        if((mods&InputEvent.BUTTON3_MASK)!=0){
            picpop.show(panel, evt.getX(), evt.getY());
        }
    }
    static JLabel reLoadPic() throws IOException{
        JLabel piclabel = new JLabel();
        piclabel.setIcon(new ImageIcon(ImageIO.read(new File("pic.png"))));
        return piclabel;
    }
}
class checkthread extends Thread{
    @Override
    public void run() {
        try {
            KKBOXreg3.checkcode();
        } catch (InterruptedException ex) {
            Logger.getLogger(checkthread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(checkthread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
}