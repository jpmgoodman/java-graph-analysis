import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Arrays;

public class MatchStudents {

    // represents a student
    private static class Student {
        private String firstName;
        private String lastName;
        private String section; // i.e., teacher's name
        private int grade; // 0 corresponds to kindergarten
    }

    // load all students from all input files
    private static ArrayList<Student> loadStudentsFromFiles(String[] filenames)
    throws FileNotFoundException {
        int numFiles = filenames.length;
        ArrayList<Student> students = new ArrayList<Student>();

        // iterate over all input files
        for (int file = 0; file < numFiles; file++) {

            // make sure file suffix matches expected name
            // ex: G3-K.csv is grade 3, class K
            String filename = filenames[file];
            String[] filesplit = filename.split("/");
            String filesuffix = filesplit[filesplit.length-1];
            if (!filesuffix.matches("G[K12345]-[a-zA-z]+\\.csv")) {
                throw new IllegalArgumentException("Invalid file name as " +
                        "input. Should be of form G[K12345]-[a-zA-z]*.");
            }

            String[] splitByDash = filesuffix.split("-");
            char gradeChar = splitByDash[0].charAt(1); 
            int currGrade = gradeChar == 'K' ? 0 : Character.getNumericValue(gradeChar);
            String currSection = (splitByDash[1]).split("\\.")[0];

            Scanner scanner = new Scanner(new File(filename));
            // file should have students in LastName, firstName order
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] name = line.split(",");
                // skip over rows without last name or first name
                if (name.length == 0) continue;
                if (name.length == 1 || name.length > 2) {
                    // throw error
                    throw new IllegalStateException("Found invalid name:" +
                            name);
                }
                // skip over rows that are just label rows
                if (name[0].compareTo("Last Name") == 0    ||
                    name[0].compareTo("Last") == 0         ||
                    name[0].compareTo("First") == 0        ||
                    name[0].compareTo("First Name") == 0)  {
                    continue;
                }
                Student student = new Student();
                student.lastName = name[0];
                student.firstName = name[1];
                student.section = currSection;
                student.grade = currGrade;

                students.add(student);
            }
            scanner.close();
        }

        return students;
    }

    // returns whether s1 and s2 are eligible to be partners;
    // currently, s1, s2 are eligible partners iff they are in
    // different sections, but the same grade
    private static boolean eligiblePartners(Student s1, Student s2) {
        return (s1.grade == s2.grade) && (s1.section != s2.section);
    }

    // create graph from students such that each node is a student,,
    // and there is an edge between two students if they don't know
    // one another (i.e., if they're in different classes)
    private static Graph createStudentGraph(ArrayList<Student> students) {
        // each student will be labeled by his/her position in the array list
        // brute force addition of edges, because data is very small
        // and only O(n^2) time, anyway
        int numStudents = students.size();
        int[][] adjMatrix = new int[numStudents][numStudents];

        // should produce symmetric matrix. don't really need to do all
        // iterations, but more clear what's happening and it doesn't
        // add to asymptotic time complexity
        for (int i = 0; i < numStudents; i++) {
            Student currStudent = students.get(i);
            for (int j = 0; j < numStudents; j++) {
                if (i == j) continue;
                Student classmate = students.get(j);
                if (eligiblePartners(currStudent, classmate)) {
                    adjMatrix[i][j] = 1;
                }
            }
        }

        return new Graph(adjMatrix);
    }

    // returns boolean array b such that b[i] = 1 iff
    // student i unmatched
    private static boolean[] getUnmatchedStudentBitArray(
            Graph studentGraph, HashSet<Edge> matching) {
        int numStudents = studentGraph.getNumVertices();
        boolean[] unmatchedStudents = new boolean[numStudents];
        Arrays.fill(unmatchedStudents, true);

        for (Edge e : matching) {
            unmatchedStudents[e.v1()] = false;
            unmatchedStudents[e.v2()] = false;
        }
        return unmatchedStudents;
    }

    private static String studentToString(Student s) {
        String grade = s.grade == 0 ? "K" : Integer.toString(s.grade);
        return "(" + s.lastName + ", " + s.firstName + ", " +
            grade + "/" + s.section + ")";
    }

    public static void main(String[] args) throws FileNotFoundException {
        ArrayList<Student> students = loadStudentsFromFiles(args);
        Graph studentGraph = createStudentGraph(students);

        // run maximum matching alg
        Blossom blossomAlg = new Blossom(studentGraph);
        HashSet<Edge> matching = blossomAlg.getMaxMatching();

        System.out.println("------------------------------");
        System.out.println("RESULTS:");
        System.out.println("------------------------------");
        System.out.println("Output format is [student1, student2]");
        System.out.println("Each student represented by " +
                "(last name, first name, grade/section)");
        System.out.println("------------------------------");
        System.out.println("MATCHED STUDENTS:");
        System.out.println("------------------------------");
        for (Edge e : matching) {
            System.out.println();
            Student s1 = students.get(e.v1());
            Student s2 = students.get(e.v2());
            System.out.println("[ " + studentToString(s1) + ",\n  " +
                    studentToString(s2) + " ]");

        }
        System.out.println();
        System.out.println("------------------------------");
        System.out.println("UNMATCHED STUDENTS:");
        boolean[] unmatchedStudents =
            getUnmatchedStudentBitArray(studentGraph, matching);
        int numUnmatched = 0;
        for (int i = 0; i < unmatchedStudents.length; i++) {
            if (unmatchedStudents[i]) {
                numUnmatched++;
                Student unmatchedStudent = students.get(i);
                System.out.println(studentToString(unmatchedStudent));
            }
        }
        if (numUnmatched == 0) {
            System.out.println("None!");
        }
        System.out.println("------------------------------");
    }
}
