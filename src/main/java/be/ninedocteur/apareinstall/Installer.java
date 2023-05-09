package be.ninedocteur.apareinstall;

import be.ninedocteur.apareinstall.org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class Installer extends JFrame implements ActionListener {
    private static final long serialVersionUID = 1L;

    private JLabel statusLabel;
    private JProgressBar progressBar;
    private JButton installButton;

    private String jarUrl = "https://github.com/9e-Docteur/ApareProject/releases/download/DEV2/ApareProject-" + checkVersionToDownload() + ".jar";
    private String runTimeURL = "https://github.com/9e-Docteur/ApareRuntime/releases/download/Release/ApareRuntime.jar";
    private String installerURL = "https://github.com/9e-Docteur/ApareInstaller/releases/download/Release/ApareInstaller.jar";
    private static String VERSION_URL = "https://raw.githubusercontent.com/9e-Docteur/ApareProject/master/version.json";
    private String installPath = "C:\\ApareProject\\Runtime\\ApareProject-" + checkVersionToDownload() + ".jar";
    private String runTimePath = "C:\\ApareProject\\Runtime\\ApareRuntime.jar";
    private String installerPath = "C:\\ApareProject\\Runtime\\ApareInstaller.jar";

    public Installer() {
        super("Apare Project Installer");

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        statusLabel = new JLabel("Click to install");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(statusLabel, BorderLayout.NORTH);

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        panel.add(progressBar, BorderLayout.CENTER);

        installButton = new JButton("Install");
        installButton.addActionListener(this);
        panel.add(installButton, BorderLayout.SOUTH);

        add(panel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == installButton) {
            installButton.setEnabled(false);
            progressBar.setValue(0);

            new Thread(new Runnable() {
                public void run() {
                    downloadFile(jarUrl, installPath);
                    downloadFile(runTimeURL, runTimePath);
                    downloadFile(installerURL, installerPath);
                    String desktopPath = System.getProperty("user.home") + "/Desktop/";
                    String jarPath = "C:/ApareProject/Runtime/ApareRuntime.jar";

                    try {
                        File shortcut = new File(desktopPath + "ApareProject.lnk");

                        FileOutputStream fos = new FileOutputStream(shortcut);

                        fos.write(getShortcutData(jarPath));

                        fos.close();

                        Process p = Runtime.getRuntime().exec("attrib +H \"" + shortcut.getAbsolutePath() + "\"");
                        p.waitFor();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private void downloadFile(String jarUrl, String installPath) {
        try {
            URL url = new URL(jarUrl);
            URLConnection conn = url.openConnection();
            int fileSize = conn.getContentLength();

            BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
            FileOutputStream fos = new FileOutputStream(installPath);

            byte[] buffer = new byte[1024];
            int count = 0;
            int totalBytesRead = 0;
            while ((count = bis.read(buffer, 0, 1024)) != -1) {
                totalBytesRead += count;
                int progress = (int) Math.round(((double) totalBytesRead / fileSize) * 100);
                progressBar.setValue(progress);

                fos.write(buffer, 0, count);
            }

            fos.close();
            bis.close();

            statusLabel.setText("Installation terminée !");
        } catch (IOException ex) {
            ex.printStackTrace();
            statusLabel.setText("Erreur lors de l'installation.");
        }

        installButton.setEnabled(true);
    }

    public double checkVersionToDownload() {
        try {
            URL url = new URL(VERSION_URL);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String jsonStr = "";
            String line;
            while ((line = reader.readLine()) != null) {
                jsonStr += line;
            }
            reader.close();

            JSONObject jsonObj = new JSONObject(jsonStr);

            double latestVersionStr = jsonObj.getDouble("version");
            return latestVersionStr;

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void update(int currentVersion){
        try {
            // Lecture du fichier JSON contenant la dernière version disponible
            URL url = new URL(VERSION_URL);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String jsonStr = "";
            String line;
            while ((line = reader.readLine()) != null) {
                jsonStr += line;
            }
            reader.close();

            JSONObject jsonObj = new JSONObject(jsonStr);

            String latestVersionStr = jsonObj.getString("version");
            double version = Double.valueOf(latestVersionStr);

            if (version > currentVersion) {
                String downloadUrl = jsonObj.getString("url");
                downloadFile(downloadUrl, installPath);
                System.out.println("Nouvelle version installée !");
            } else {
                System.out.println("Vous avez déjà la dernière version.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] getShortcutData(String target) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);

        out.writeShort(0x4C);
        out.writeShort(0x00);

        out.writeShort(0x1C + 2 * target.length());
        out.writeShort(0x00);

        out.writeInt(0x0000001F);

        out.writeBytes(target);
        out.writeByte(0x00);

        out.writeBytes("");
        out.writeByte(0x00);

        out.writeBytes("");
        out.writeByte(0x00);

        out.writeBytes("");
        out.writeByte(0x00);

        return stream.toByteArray();
    }

    public static void main(String[] args) {
        new Installer();
    }
}
