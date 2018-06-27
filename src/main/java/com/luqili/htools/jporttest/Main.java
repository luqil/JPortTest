package com.luqili.htools.jporttest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;

public class Main {
    
    public static void main(String[] args) {
        String path = System.getProperty("user.home");
        System.out.println("JProtTest Tool");
        File f = new File(path + "/port_test");
        if (!f.exists()) {
            f.mkdirs();
            File f1 = new File(f, "help.txt");
            try (FileWriter fw = new FileWriter(f1)) {
                fw.write("目录[~/port_test]内,以[address-timeout-cpuCount-.pt]形式的文件，正则表达式匹配[^[0-9a-zA-Z.]{1,100}-[0-9]{1,5}-[0-9]{1,3}-.pt$]，为扫描文件。");
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        } else {
            File[] fs = f.listFiles();
            for (File pf : fs) {
                String filename = pf.getName();
                String[] pp = filename.split("-");
                String address = pp[0];
                if (!Pattern.matches("^[0-9a-zA-Z.]{1,100}-[0-9]{1,5}-[0-9]{1,3}-.pt$", filename)) {
                    continue;
                }
                Integer timeout = Integer.parseInt(pp[1]);
                Integer cpuCount = Integer.parseInt(pp[2]);
                threadWork(pf, address, timeout, cpuCount);
            }
        }
        System.out.println(f.getAbsolutePath());
    }
    
    public static void threadWork(File file, String address, int timeOut, int threadCount) {
        try (FileWriter fw = new FileWriter(file,true)) {
            Date d = new Date();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String line = "\r\n" + df.format(d) + "查询:\r\n";
            fw.append(line);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        int avg = 65535 / threadCount;
        avg = 65535 % threadCount > 0 ? avg + 1 : avg;
        List<List<Integer>> prots = new ArrayList<>();
        int port = 0;
        for (int i = 0; i < threadCount; i++) {
            List<Integer> ps = new ArrayList<>();
            for (int j = 0; j < avg; j++) {
                if (port < 65536) {
                    ps.add(port++);
                } else {
                    break;
                }
            }
            prots.add(ps);
        }
        CountDownLatch cd = new CountDownLatch(prots.size());// 等待线程执行
        for (List<Integer> ps : prots) {
            Thread th = new Thread(() -> {
                for (int p : ps) {
                    boolean result = testPort(address, p, timeOut);
                    if (result) {
                        try (FileWriter fw = new FileWriter(file,true)) {
                            fw.append(p + ",");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println(address + ":" + p + ":" + result);
                }
                cd.countDown();
            });
            th.start();
        }
        try {
            cd.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try (FileWriter fw = new FileWriter(file,true)) {
            Date d = new Date();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String line = "\r\n" + df.format(d) + "查询完成。\r\n";
            fw.append(line);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static boolean testPort(String address, int port, int timeOut) {
        try {
            Socket s = new Socket();
            s.connect(new InetSocketAddress(address, port), timeOut);
            s.close();
            return true;
        } catch (UnknownHostException e) {
        } catch (IOException e) {
        }
        return false;
    }
}
