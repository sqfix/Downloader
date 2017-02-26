
import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Makc on 25.02.2017.
 */
public class Downloader {
    private int countThreads;
    private int limitSpeed;
    private String pathSource;
    private String pathTo;

    ArrayList<String> urls = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        inity(args[0]);
        //inity("java -jar utility.jar -n 2 -l 2000k -o E:\\Users\\Makc\\Documents\\GitHub\\Downloader\\downloaded -f links.txt");
    }

    public static void inity(String s)
    {
        int n;
        int l;
        String f;
        String o;
        n = Integer.parseInt(s.substring(s.lastIndexOf("-n") + 3, s.lastIndexOf("-n") + 4));
        l = Integer.parseInt(s.substring(s.lastIndexOf("-l") + 3, s.lastIndexOf("-l") + 7)) * 1024;
        o = s.substring(s.lastIndexOf("-o") + 3, s.lastIndexOf(' ') - 3);
        f = s.substring(s.lastIndexOf(' ') + 1);
        //System.out.println(new Formatter().format(" n = %d \n l = %d \n f = %s \n o = %s", n, l, f, o));

        Downloader downloader = new Downloader(n, l, f, o);
        downloader.startDownload();
    }

    public Downloader(int countThreads, int limitSpeed, String pathSource, String pathTo) {
        this.countThreads = countThreads;
        this.limitSpeed = limitSpeed;
        this.pathSource = pathSource;
        this.pathTo = pathTo;

        readLinks(pathSource);
    }

    private void readLinks(String s) {
        try(BufferedReader reader = new BufferedReader(new FileReader(new File(s))))
        {
            while (reader.ready())
            {
                urls.add(reader.readLine());
            }
        }catch (FileNotFoundException e){
            System.out.println("FILE IS NOT FOUND");
        }catch (IOException e){
            System.out.println("EXCEPTION");
        }
    }

    public void startDownload()
    {
        final ExecutorService executorService = Executors.newFixedThreadPool(countThreads);
        for(final String url : urls)
        {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    String fileName = addFileName(url);
                    File fileTo = new File(pathTo + "\\" + fileName);
                    try (ReadableByteChannel rbc = Channels.newChannel(new URL(url).openStream());
                            FileOutputStream fos = new FileOutputStream(String.valueOf(fileTo)))
                    {
                        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                    }catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private synchronized String addFileName(String url) {
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        String wFormat = fileName.substring(0, fileName.lastIndexOf("."));
        String format = fileName.substring(fileName.lastIndexOf("."));
        while (true){
            if(check(fileName))
            {
                fileName = wFormat + new Random().nextInt(999) + format;
            }else {
                return fileName;
            }
        }
    }

    private synchronized boolean check (String fileName)
    {
        return new File(pathTo + "\\" + fileName).exists();
    }


}
