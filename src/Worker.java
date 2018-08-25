
import java.io.PrintWriter;
import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import site.Thongtincongty;
import util.CollaborationQueue;
import util.Crawler;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author hovinhthinh
 */
public class Worker extends Thread {

    PrintWriter out;
    CollaborationQueue<URL> queue;
    AtomicInteger pagesCount;
    boolean useProxy;
    Main parentWindows;

    public Worker(CollaborationQueue<URL> queue, PrintWriter out, AtomicInteger pagesCount, boolean useProxy, Main parentWindows) {
        this.queue = queue;
        this.out = out;
        this.pagesCount = pagesCount;
        this.useProxy = useProxy;
        this.parentWindows = parentWindows;
    }

    @Override
    public void run() {
        URL url;
        while ((url = queue.pop()) != null) {
            String content = Crawler.getContentFromUrl(url.toString());
            if (content == null) {
                continue;
            }
            pagesCount.incrementAndGet();
            
            Thongtincongty cty = Thongtincongty.parseFromPageContent(url, content);
            if (cty != null) {
                synchronized (out) {
                    out.println(cty.toTSVLine());
                    out.flush();                    
                }
                parentWindows.addExportedRecord(cty);
            }
            List<URL> outLinks = Thongtincongty.extractOutLinksFromPageContent(url, content);
            if (outLinks != null) {
                for (URL u : outLinks) {
                    queue.push(u);
                }
            }
        }
    }
}
