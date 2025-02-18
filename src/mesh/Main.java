package mesh;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Scanner;

public class Main {

    public static final char[] HEADER = {0x76, 0x65, 0x72, 0x73, 0x69, 0x6F, 0x6E, 0x20, 0x32, 0x2E, 0x30, 0x30, 0x0A, 0x0C, 0x00, 0x24, 0x0C};

    public static void main(String[] args) {
        try (Scanner reader = new Scanner(System.in)) {
            FileInputStream file = loadFile(reader);

            String option = getOutputOption(reader);

            processFile(file, option);

            System.out.println("Goodbye!");
        }
    }

    private static FileInputStream loadFile(Scanner reader) {
        FileInputStream file = null;
        while (file == null) {
            System.out.println("File path: ");
            String path = reader.nextLine();
            try {
                file = openFile(path);
            } catch (IOException e) {
                System.err.println("An error occurred while loading the file: " + e.getMessage());
            }
        }
        return file;
    }

    private static String getOutputOption(Scanner reader) {
        String option;
        do {
            System.out.println("Output as file? (yes/no)");
            option = reader.nextLine();
        } while (!option.equals("yes") && !option.equals("no"));
        return option;
    }

    private static void processFile(FileInputStream file, String option) {
        try {
            long start = System.currentTimeMillis();

            ByteBuffer bb = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
            file.read(bb.array());
            int triadCount = bb.getInt();
            int polyCount = bb.getInt();

            Triad[] triads = readTriads(file, triadCount);
            Poly[] polys = readPolys(file, polyCount, triads);

            long time = System.currentTimeMillis() - start;

            doMagicWithData(polys);

            doPrintAsVersionOne(polys, option, time, triadCount, polyCount);

        } catch (IOException e) {
            System.err.println("An error occurred while parsing the file: " + e.getMessage());
        } finally {
            try {
                file.close();
            } catch (IOException e) {
                System.err.println("Failed to close the file: " + e.getMessage());
            }
        }
    }

    private static Triad[] readTriads(FileInputStream file, int triadCount) throws IOException {
        Triad[] triads = new Triad[triadCount];
        for (int i = 0; i < triadCount; i++) {
            triads[i] = new Triad(file);
        }
        return triads;
    }

    private static Poly[] readPolys(FileInputStream file, int polyCount, Triad[] triads) throws IOException {
        byte[] footer = new byte[polyCount * 3 * 4];
        file.read(footer);

        ByteBuffer bb = ByteBuffer.wrap(footer).order(ByteOrder.LITTLE_ENDIAN);
        Poly[] polys = new Poly[polyCount];
        for (int i = 0; i < polyCount; i++) {
            polys[i] = new Poly(triads[bb.getInt()], triads[bb.getInt()], triads[bb.getInt()]);
        }
        return polys;
    }

    private static FileInputStream openFile(String path) throws IOException {
        FileInputStream file = new FileInputStream(path);
        byte[] header = new byte[HEADER.length];
        if (file.read(header) != HEADER.length || !checkHeader(header)) {
            file.close();
            throw new IOException("The header of the input file was not correct.");
        }
        return file;
    }

    private static boolean checkHeader(byte[] header) {
        for (int i = 0; i < HEADER.length; i++) {
            if (header[i] != HEADER[i]) {
                return false;
            }
        }
        return true;
    }

    private static void doPrintAsVersionOne(Poly[] polys, String option, long time, int triadCount, int polyCount) throws FileNotFoundException {
        System.out.println("---- BEGIN DECODED MESH ----");

        try (PrintStream out = option.equals("yes") ? new PrintStream(new FileOutputStream("output.mesh")) : System.out) {
            if (option.equals("yes")) {
                System.out.println("(Output differed to file 'output.mesh')");
            }

            out.println("version 1.00");
            out.println(polyCount);

            StringBuilder sb = new StringBuilder();
            for (Poly p : polys) {
                sb.append(p.a).append(p.b).append(p.c);
            }
            out.print(sb.toString());

            System.out.println("\n---- END DECODED MESH ----");
            System.out.println("Processed " + triadCount + " triads, composing of " + polyCount + " poly(s) in " + (time / 1000.0) + " seconds.");
        }
    }

    /**
     * README:
     *
     * At this point in the code the file has been parsed. All of the polygons
     * have been inserted into the polys[] array for you to use, manipulate, edit,
     * or whatever you want. You can use this stub method do whatever you want.
     */
    private static void doMagicWithData(Poly[] polys) {
        // TODO Auto-generated method stub
    }
		}
