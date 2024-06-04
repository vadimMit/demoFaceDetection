package com.example.demo.services;

import nu.pattern.OpenCV;
import org.apache.tomcat.util.codec.binary.Base64;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class detectFace {
    private static final String startPath = "D:\\ideaProjects\\demo\\demo\\src\\main\\resources\\faces\\";
    private static final String originalPath = "D:\\ideaProjects\\demo\\demo\\src\\main\\resources\\originalFace\\";
    private static final String compareFileName = "toCompare.png";
    private static final String comparePath = "D:\\ideaProjects\\demo\\demo\\src\\main\\resources\\toCompare\\";
    private static CascadeClassifier cascadeClassifier;
    private static ArrayList<String> filesNames = new ArrayList<>();
    static {
        OpenCV.loadShared();
        cascadeClassifier = new CascadeClassifier("D:\\ideaProjects\\demo\\demo\\src\\main\\resources\\haarcascades\\haarcascade_frontalface_alt.xml");
    }
    public static synchronized void save(MultipartFile file) throws IOException {
        File fileToSave = new File(startPath + file.getOriginalFilename());
        filesNames.add(file.getOriginalFilename());
        file.transferTo(fileToSave);
    }

    public static synchronized void saveOriginal(MultipartFile file) throws IOException {
        File cleanFile = new File(originalPath);
        FileUtils.cleanDirectory(cleanFile);
        File fileToSave = new File(originalPath + file.getOriginalFilename());
        filesNames.add(file.getOriginalFilename());
        file.transferTo(fileToSave);
    }

    public static synchronized Double saveScreenShootAndCompare(String file) throws IOException {
        File cleanFile = new File(comparePath);
        FileUtils.cleanDirectory(cleanFile);
        byte[] data = Base64.decodeBase64(file);
        try (OutputStream stream = new FileOutputStream(comparePath + compareFileName)) {
            stream.write(data);
        }
        return detectFace.compareWithOriginal() * 100;
    }

    private static synchronized Double compareWithOriginal() {
        File folder = new File(originalPath);
        File[] listOfFiles = folder.listFiles();
        String originalFileName = listOfFiles[0].getName();
        Mat mat_1 = detectFaceFromVideo(originalPath + originalFileName);
        System.out.println(mat_1);
        Mat mat_2 = detectFaceFromVideo(comparePath + compareFileName);
        System.out.println(mat_2);
        Mat hist_1 = new Mat();
        Mat hist_2 = new Mat();

        // Color range
        MatOfFloat ranges = new MatOfFloat(0f, 256f);
        // Histogram size, the bigger the matching, the more accurate (slower)
        MatOfInt histSize = new MatOfInt(10000000);

        Imgproc.calcHist(Arrays.asList(mat_1), new MatOfInt(0), new Mat(), hist_1, histSize, ranges);
        Imgproc.calcHist(Arrays.asList(mat_2), new MatOfInt(0), new Mat(), hist_2, histSize, ranges);

        // CORREL correlation coefficient
        double res = Imgproc.compareHist(hist_1, hist_2, Imgproc.CV_COMP_CORREL);

        return res;
    }

    private static synchronized Mat detectFaceFromVideo(String filePath) {
        Mat image0 = loadImage(filePath);
        Mat image1 = new Mat();
        Imgproc.cvtColor(image0, image1, Imgproc.COLOR_BGR2GRAY);
        MatOfRect facesDetected = new MatOfRect();
        cascadeClassifier.detectMultiScale(image0,facesDetected);
        Rect[] facesArray = facesDetected.toArray();
        for(Rect rect : facesArray) {
            Mat face = new Mat(image1, rect);
            return face;
        }
        return null;
    }





    public static synchronized Double compare() throws IOException {
        File f = new File(startPath);
        int count = 0;
        for (File fileInFolder : f.listFiles()) {
            if (fileInFolder.isFile()) {
                count++;
            }
        }
        if (count == 2) {
            Double total = detectFace.compareFaces();
            filesNames.clear();
            File cleanFile = new File(startPath);
            FileUtils.cleanDirectory(cleanFile);
            return total * 100;
        } else return 0.0;
    }

    private static synchronized Double compareFaces() {
        Mat mat_1 = detectFace(filesNames.get(0));
        System.out.println(mat_1);
        Mat mat_2 = detectFace(filesNames.get(1));
        System.out.println(mat_2);
        Mat hist_1 = new Mat();
        Mat hist_2 = new Mat();

        // Color range
        MatOfFloat ranges = new MatOfFloat(0f, 256f);
        // Histogram size, the bigger the matching, the more accurate (slower)
        MatOfInt histSize = new MatOfInt(10000000);

        Imgproc.calcHist(Arrays.asList(mat_1), new MatOfInt(0), new Mat(), hist_1, histSize, ranges);
        Imgproc.calcHist(Arrays.asList(mat_2), new MatOfInt(0), new Mat(), hist_2, histSize, ranges);

        // CORREL correlation coefficient
        double res = Imgproc.compareHist(hist_1, hist_2, Imgproc.CV_COMP_CORREL);

        return res;
    }

    private static synchronized Mat detectFace(String fileName) {
        String sourceImagePath = startPath + fileName;
        Mat image0 = loadImage(sourceImagePath);
        System.out.println(image0);
        Mat image1 = new Mat();
        Imgproc.cvtColor(image0, image1, Imgproc.COLOR_BGR2GRAY);
        MatOfRect facesDetected = new MatOfRect();
        cascadeClassifier.detectMultiScale(image0,facesDetected);
        Rect[] facesArray = facesDetected.toArray();
        for(Rect rect : facesArray) {
            Mat face = new Mat(image1, rect);
            return face;
        }
        return null;
    }
    private static synchronized Mat loadImage(String imagePath) {
        Imgcodecs imageCodecs = new Imgcodecs();
        return imageCodecs.imread(imagePath);
    }

}
