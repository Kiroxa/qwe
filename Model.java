
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Model {
    private int rectangleWidth;
    private int rectangleHeight;
    private double error;
    private int p;
    private BufferedImage image;
    private BufferedImage restoredImage;
    private List<Rectangle> rectangles;
    private double learningRate;
    private double learningRate_;
    private Matrix W;
    private Matrix W_;
    private Matrix X;
    private Matrix X_;
    private Matrix Y;
    private Matrix deltaX;
    private boolean randomize_weights = true;
    private boolean first_learn = true;


    public void start(int mode) {
        splitImageIntoRectangles();

        if (randomize_weights) {
            createFirstLayerWeightsMatrix();
            createSecondLayerWeightsMatrix();
            randomize_weights = false;
        }
        
        if (mode == 0)
        {
            if (!first_learn)
            {
                loadWFromFile();
                loadW_FromFile();
            } else {
                first_learn = false;
            }
            learn();
        } else if (mode == 1) {
            loadWFromFile();
            loadW_FromFile();
            work();
            saveRectanglesVectorX0InFile();
        } else if (mode == 2) {
            loadWFromFile();
            loadW_FromFile();
            loadRectanglesVectorX0FromFile();
            restoreImage();
        }
    }

    public void setRectangleWidth(int width) {
        this.rectangleWidth = width;
    }

    public void setRectangleHeight(int rectangleHeight) {
        this.rectangleHeight = rectangleHeight;
    }

    public void setError(double error) {
        this.error = error;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public void setP(int p) {
        this.p = p;
    }

    private void splitImageIntoRectangles() {
        rectangles = new ArrayList<>();
        int x = 0;
        while (x < image.getWidth()) {
            int y = 0;
            while (y < image.getHeight()) {
                Rectangle rectangle = new Rectangle(x, y);
                for (int i = x; i < x + rectangleWidth; i++) {
                    for (int j = y; j < y + rectangleHeight; j++) {
                        if (i < image.getWidth() && j < image.getHeight()) {
                            Color color = new Color(image.getRGB(i, j));
                            rectangle.addPixel(color);
                        } else {
                            rectangle.add(-1);
                            rectangle.add(-1);
                            rectangle.add(-1);
                        }
                    }
                }
                rectangle.createVectorX0();
                rectangles.add(rectangle);
                y = y + rectangleHeight;
            }
            x = x + rectangleWidth;
        }

    }

    private void createFirstLayerWeightsMatrix() {
        double randomWeights[][] = new double[rectangleWidth * rectangleHeight * 3][p];
        for (int row = 0; row < rectangleWidth * rectangleHeight * 3; row++) {
            for (int column = 0; column < p; column++) {
                randomWeights[row][column] = Math.random() * 2 - 1;
            }
        }
        W = new Matrix(randomWeights);
    }

    private void createSecondLayerWeightsMatrix() {
        W_ = W.transpose();
    }

    private void learn() {
        int iteration = 0;
        double E = Double.MAX_VALUE;
        while (E > error) {
            E = 0;
            for (Rectangle pattern : rectangles) {
                X = pattern.getVectorX0();
                Y = X.multiply(W);
                X_ = Y.multiply(W_);
                deltaX = X_.subtract(X);
                adaptLearningRate();
                correctWeights();
                E += calculateError();
            }
            iteration++;
            System.out.println("Iteration = " + iteration + "; Error = " + E);
        }
        System.out.println("First layer weights: \n ");
        W.print();
        saveWInFile();
        System.out.println("Second layer weights: \n ");
        W_.print();
        saveW_InFile();
    }

    private void work() {
        for (Rectangle pattern : rectangles) {
            X = pattern.getVectorX0();
            Y = X.multiply(W);
            X_ = Y.multiply(W_);
            deltaX = X_.subtract(X);
        }
    }
    
    private void saveWInFile() {
        try {
            FileWriter writer = new FileWriter("W.txt", false);
            for (int row = 0; row < W.getRows(); row++) {
                for (int column = 0; column < W.getColumns(); column++) {
                    writer.write(W.get(row, column) + " ");
                }
                writer.write("\n");
            }
            writer.flush();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void saveW_InFile() {
        try {
            FileWriter writer = new FileWriter("W_.txt", false);
            for (int row = 0; row < W_.getRows(); row++) {
                for (int column = 0; column < W_.getColumns(); column++) {
                    writer.write(W_.get(row, column) + " ");
                }
                writer.write("\n");
            }
            writer.flush();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    private void loadWFromFile() {
        try {
            FileReader reader = new FileReader("W.txt");
            int character;
            String str = "";
            int row = 0;
            while ((character = reader.read()) != -1) {
                if (character != 10) {
                    str += (char) character;
                } else {
                    String[] strArray = str.split(" ");
                    for (int column = 0; column < strArray.length; column++) {
                        W.set(row, column, Double.parseDouble(strArray[column]));
                    }
                    str = "";
                    row++;
                }
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void loadW_FromFile() {
        try {
            FileReader reader = new FileReader("W_.txt");
            int character;
            String str = "";
            int row = 0;
            while ((character = reader.read()) != -1) {
                if (character != 10) {
                    str += (char) character;
                } else {
                    String[] strArray = str.split(" ");
                    for (int column = 0; column < strArray.length; column++) {
                        W_.set(row, column, Double.parseDouble(strArray[column]));
                    }
                    str = "";
                    row++;
                }
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void adaptLearningRate() {
        double someValueX = 1000;
        for (int i = 0; i < X.getMatrix()[0].length; i++) {
            someValueX += X.getMatrix()[0][i] * X.transpose().getMatrix()[i][0];
        }
        learningRate = 1 / someValueX;

        double someValueY = 1000;
        for (int i = 0; i < Y.getMatrix()[0].length; i++) {
            someValueY += Y.getMatrix()[0][i] * Y.transpose().getMatrix()[i][0];
        }
        learningRate_ = 1 / someValueY;
    }

    private void correctWeights() {
        W = W.subtract(X.transpose().multiply(learningRate).multiply(deltaX).multiply(W_.transpose()));
        W_ = W_.subtract(Y.transpose().multiply(learningRate_).multiply(deltaX));
        normaliseWeights();
    }

    private void normaliseWeights() {
        for (int i = 0; i < W.getMatrix().length; i++) {
            double value = 0;
            for (int j = 0; j < W.getMatrix()[0].length; j++) {
                value += W.getMatrix()[i][j] * W.getMatrix()[i][j];
            }
            value = Math.sqrt(value);
            for (int j = 0; j < W.getMatrix()[0].length; j++) {
                W.getMatrix()[i][j] = W.getMatrix()[i][j] / value;
            }
        }
        for (int i = 0; i < W_.getMatrix().length; i++) {
            double value = 0;
            for (int j = 0; j < W_.getMatrix()[0].length; j++) {
                value += W_.getMatrix()[i][j] * W_.getMatrix()[i][j];
            }
            value = Math.sqrt(value);
            for (int j = 0; j < W_.getMatrix()[0].length; j++) {
                W_.getMatrix()[i][j] = W_.getMatrix()[i][j] / value;
            }
        }
    }

    private double calculateError() {
        double e = 0;
        for (int i = 0; i < X.getMatrix()[0].length; i++) {
            e += deltaX.getMatrix()[0][i] * deltaX.getMatrix()[0][i];
        }
        return e;
    }

    private double restorePixel(double RGB) {
        double value = 255 * (RGB + 1) / 2;
        if (value < 0) {
            value = 0;
        } else if (value > 255) {
            value = 255;
        }
        return value;
    }

    public void saveRectanglesVectorX0InFile() {
        try {
            FileWriter writer = new FileWriter("RectanglesVectorX0.txt", false);
            for (Rectangle rectangle : rectangles) {
                for (int row = 0; row < rectangle.getVectorX0().getRows(); row++) {
                    for (int column = 0; column < rectangle.getVectorX0().getColumns(); column++) {
                        writer.write(rectangle.getVectorX0().get(row, column) + " ");
                    }
                    writer.write("\n");
                }
                writer.write("\n");
            }
            writer.flush();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void loadRectanglesVectorX0FromFile() {
        try {
            FileReader reader = new FileReader("RectanglesVectorX0.txt");
            int character;
            String str = "";
            int row = 0;
            int column = 0;
            int rectangleNumber = 0;
            while ((character = reader.read()) != -1) {
                if (character != 10) {
                    str += (char) character;
                } else {
                    if (str.equals("")) {
                        rectangleNumber++;
                        row = 0;
                        column = 0;
                    } else {
                        for (String s: str.split(" ")) {
                            rectangles.get(rectangleNumber).getVectorX0().set(row, column, Double.parseDouble(s));
                            str = "";
                            column++;
                            if (column == rectangles.get(rectangleNumber).getVectorX0().getColumns()) {
                                column = 0;
                                row++;
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void restoreImage() {
        restoredImage = new BufferedImage(256, 256, BufferedImage.TYPE_3BYTE_BGR);
        for (Rectangle rectangle : rectangles) {
            X = rectangle.getVectorX0();
            Y = X.multiply(W);
            X_ = Y.multiply(W_);
            int x = rectangle.getX();
            int y = rectangle.getY();
            int pixelPosition = 0;
            for (int i = 0; i < rectangleWidth; i++) {
                for (int j = 0; j < rectangleHeight; j++) {
                    int red = (int) restorePixel(X_.getMatrix()[0][pixelPosition++]);
                    int green = (int) restorePixel(X_.getMatrix()[0][pixelPosition++]);
                    int blue = (int) restorePixel(X_.getMatrix()[0][pixelPosition++]);
                    Color color = new Color(red, green, blue);
                    if (x + i < image.getWidth()) {
                        if (y + j < image.getHeight()) {
                            restoredImage.setRGB(x + i, y + j, color.getRGB());
                        }
                    }
                }
            }
        }
    }

    public BufferedImage getRestoredImage() {
        return restoredImage;
    }

    public int getNumOfRectangles() {
        return rectangles.size();
    }
}
