import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Controller {
    private int height;
    private int width;
    private int p;
    private double error;
    private int L;
    BufferedImage image;
    BufferedImage restoredImage;
    Model model;
    
    File[] images_for_learning = {
        new File("C:\\Users\\kiril\\Desktop\\MRZvIS_Lab_1\\MRZvIS_Lab_1\\images\\256-gradient.png"),
        new File("C:\\Users\\kiril\\Desktop\\MRZvIS_Lab_1\\MRZvIS_Lab_1\\images\\Bvckup_2_-_Application_icon.png"),
        new File("C:\\Users\\kiril\\Desktop\\MRZvIS_Lab_1\\MRZvIS_Lab_1\\images\\feeds-green-256-256.png"),
        new File("C:\\Users\\kiril\\Desktop\\MRZvIS_Lab_1\\MRZvIS_Lab_1\\images\\logo-volkswagen-256-256.png"),
    };

    public Controller(int mode) throws IOException {
        if (mode == 0) { // learn
            inputParameters();
            
            for (File image_for_learning : images_for_learning) {
                try {
                    image = ImageIO.read(image_for_learning);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                setParametersToModel();
                model.start(0);
            }
        
            printParameters();   
            
        } else if (mode == 1) { // work
            inputImage();
            loadParametersFromFile();
            setParametersToModel();
            model.start(1);
        } else if (mode == 2) {
            image = ImageIO.read(new File("C:\\Users\\kiril\\Desktop\\MRZvIS_Lab_1\\MRZvIS_Lab_1\\images\\256-gradient.png"));
            loadParametersFromFile();
            setParametersToModel();
            model.start(2);
            saveImage();
        }
    }

    private void setParametersToModel(){
        this.model = new Model();
        model.setImage(image);
        model.setError(error);
        model.setP(p);
        model.setRectangleHeight(height);
        model.setRectangleWidth(width);
    }

    private void inputImage() {
                JFrame frame = new JFrame();
                JFileChooser chosenFile = new JFileChooser("images");
                FileNameExtensionFilter filter = new FileNameExtensionFilter("*.png", new String[]{"png"});
                chosenFile.setFileFilter(filter);
                int ret = chosenFile.showOpenDialog(frame);
                if (ret == 0) {
                    File file = chosenFile.getSelectedFile();
                    try {
                image = ImageIO.read(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void inputParameters() {
        Scanner scanner = new Scanner(System.in, "UTF-8");
        System.out.println("Rectangle's height (m) :");
        this.height = scanner.nextInt();
        System.out.println("Rectangle's width (n) :");
        this.width = scanner.nextInt();
        System.out.println("Number of neurons on the second layer (p) :");
        this.p = scanner.nextInt();
        System.out.println("Max error value (e) :");
        this.error = scanner.nextInt();
        saveParametersInFile();
    }

    private void saveParametersInFile() {
        try {
            FileWriter writer = new FileWriter("parameters.txt", false);
            writer.write(height + "\n");
            writer.write(width + "\n");
            writer.write(p + "\n");
            writer.write(error + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }   
    }
    
    private void loadParametersFromFile() {
        try {
            Scanner scanner = new Scanner(new File("parameters.txt"));
            while(scanner.hasNextLong()) {
                this.height = scanner.nextInt();
                this.width = scanner.nextInt();
                this.p = scanner.nextInt();
                this.error = scanner.nextFloat();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
        
    public void printParameters() {
        int N = width * height * 3;
        L = model.getNumOfRectangles();
        double Z = (N * this.L) / ((N + this.L) * this.p + 2.0);
        System.out.println("n = " + this.width);
        System.out.println("m = " + this.height);
        System.out.println("p = " + this.p);
        System.out.println("Z = " + Z);
        System.out.println("L = " + this.L);
    }

    private void ParametersToModel(){
        this.model = new Model();
        model.setImage(image);
        model.setError(error);
        model.setP(p);
        model.setRectangleHeight(height);
        model.setRectangleWidth(width);
    }

    private void saveImage(){
        restoredImage = model.getRestoredImage();
        File file = new File("images/output.png");
        try{
            ImageIO.write(restoredImage,"png", file);
        } catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("Image saved.");
    }

}
