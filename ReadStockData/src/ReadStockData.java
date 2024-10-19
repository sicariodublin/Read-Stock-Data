import java.io.*;
import java.util.*;

public class ReadStockData {
    // List to hold the data read from the CSV file
    private static List<String[]> data = new ArrayList<>();

    public static void main(String[] args) {
        // Step 1: Read CSV file into memory using BufferedReader
        String fileName = "C:/Documents/NetBeansProjects/ReadStockData/stock.csv";  // Update this path as needed
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) { // Open the file for reading
            String line; // Variable to hold each line read from the file
            boolean isHeader = true; // Flag to skip the header row
            while ((line = br.readLine()) != null) { // Read lines until the end of the file
                if (isHeader) { // Check if it's the header row
                    isHeader = false; // Skip the header row
                    continue; // Continue to the next iteration
                }
                data.add(line.split(",")); // Split the line by commas and add to the data list
            }
        } catch (IOException e) { // Handle any IO exceptions
            e.printStackTrace(); // Print the stack trace for debugging
        }

        // Accept a new record from the user and add it to the end of the record array
        Scanner scanner = new Scanner(System.in);
        boolean validInput = false;

        while (!validInput) {
            System.out.println();
            System.out.println("Enter new record details:");

            try {
                System.out.print("Enter product name: ");
                String name = scanner.nextLine();
                if (name.isEmpty() || name.matches("\\d+")) {
                    throw new InvalidProductNameException("Product product_name cannot be empty. It cannot have only digits! Please correct this message. Type your new record again.");
                }

                System.out.print("Enter category: ");
                String category = scanner.nextLine();

                System.out.print("Enter quantity: ");
                int quantity = Integer.parseInt(scanner.nextLine());

                System.out.print("Enter price: ");
                double price = Double.parseDouble(scanner.nextLine());

                int newId = data.size() + 1; // New consecutive stock_no number
                String[] newRecord = {String.valueOf(newId), name, category, String.valueOf(quantity), String.format("%.2f", price)};
                data.add(newRecord);

                System.out.println("New record added successfully.");
                System.out.println("");
                printData(data);
                saveToFile(data, fileName);
                validInput = true;

            } catch (InvalidProductNameException e) {
                System.out.println(e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format. Please enter valid numeric values for quantity and price.");
            }
        }

        // Step 2: Identify and sum the even-numbered quantities
        List<String[]> evenQuantityRows = new ArrayList<>(); // List to hold rows with even quantities
        int sum = sumEvenQuantities(data, 3, evenQuantityRows); // Calculate the sum of even quantities, assuming Quantity is in column 3
        System.out.println();
        System.out.println("Rows with even quantities:"); // Print message
        printData(evenQuantityRows); // Print the rows with even quantities
        System.out.println("Total sum of even quantities: " + sum); // Print the total sum of even quantities
        System.out.println();

        // Step 3: Iterative method to find the minimum value in the Quantity column from odd-indexed rows
        int min = findMinOddIndexedRows(data, 3); // Calculate the minimum value in the Quantity column for odd-indexed rows
        System.out.println("Minimum value in odd-indexed rows: " + min); // Print the minimum value
        System.out.println();

        // Step 4: Provide sorting options for all columns in the data file
        System.out.println("Enter column to sort by (ID, Name, Category, Quantity, Price):"); // Prompt for column name
        String column = scanner.nextLine(); // Read the column name
        System.out.println("Enter order (asc/desc):"); // Prompt for sorting order
        String order = scanner.nextLine(); // Read the order

        if (column.equalsIgnoreCase("Category")) { // Check if the column is "Category"
            System.out.println("Do you want to sort by a specific category? (yes/no):"); // Prompt for specific category
            String specificCategory = scanner.nextLine(); // Read the specific category choice
            if (specificCategory.equalsIgnoreCase("yes")) { // If user wants to sort by specific category
                System.out.println("Enter specific category to sort by (Accessories, Clothing, Electronics, Household):"); // Prompt for specific category
                String category = scanner.nextLine(); // Read the category
                sortAndPrintData(data, column, order, category); // Sort and print data by specific category
            } else {
                sortAndPrintData(data, column, order, null); // Sort and print data without specific category
            }
        } else {
            sortAndPrintData(data, column, order, null); // Sort and print data for the specified column
        }

        // Step 5: Create a multi-threaded solution to sort and save data
        createAndRunSortingThreads(data); // Create and run threads for sorting data and saving to files
    }

    // Method to sum even quantities and collect rows with even quantities
    private static int sumEvenQuantities(List<String[]> data, int colIndex, List<String[]> evenQuantityRows) {
        int sum = 0; // Initialize sum to 0
        for (String[] row : data) { // Iterate through each row in the data
            int quantity = Integer.parseInt(row[colIndex]); // Parse the quantity from the specified column
            if (quantity % 2 == 0) { // Check if the quantity is even
                evenQuantityRows.add(row); // Add the row to the list of even quantity rows
                sum += quantity; // Add the quantity to the sum
            }
        }
        return sum; // Return the total sum of even quantities
    }

    // Method to find the minimum value in the Quantity column from odd-indexed rows
    private static int findMinOddIndexedRows(List<String[]> data, int colIndex) {
        int min = Integer.MAX_VALUE; // Initialize min to the maximum integer value
        for (int i = 1; i < data.size(); i += 2) { // Iterate through odd-indexed rows
            int value = Integer.parseInt(data.get(i)[colIndex]); // Parse the quantity from the specified column
            if (value < min) min = value; // Update min if a smaller value is found
        }
        return min; // Return the minimum value
    }

    // Method to sort and print data based on user input
    private static void sortAndPrintData(List<String[]> data, String column, String order, String specificCategory) {
        Comparator<String[]> comparator; // Comparator for sorting

        // Determine comparator based on column name
        switch (column.toLowerCase()) {
            case "id":
                comparator = Comparator.comparingInt(o -> Integer.parseInt(o[0]));
                break;
            case "name":
                comparator = Comparator.comparing(o -> o[1]);
                break;
            case "category":
                comparator = Comparator.comparing(o -> o[2]);
                break;
            case "quantity":
                comparator = Comparator.comparingInt(o -> Integer.parseInt(o[3]));
                break;
            case "price":
                comparator = Comparator.comparingDouble(o -> Double.parseDouble(o[4]));
                break;
            default:
                System.out.println("Invalid column name.");
                return;
        }

        if ("desc".equalsIgnoreCase(order)) { // If order is descending
            comparator = comparator.reversed(); // Reverse the comparator for descending order
        }

        if (specificCategory != null) { // If sorting by specific category
            List<String[]> filteredData = new ArrayList<>(); // List to hold filtered data
            for (String[] row : data) { // Iterate through each row
                if (row[2].equalsIgnoreCase(specificCategory)) { // Check if the category matches
                    filteredData.add(row); // Add row to filtered data
                }
            }
            filteredData.sort(comparator); // Sort the filtered data
            printData(filteredData); // Print the sorted filtered data
        } else {
            data.sort(comparator); // Sort the entire data
            printData(data); // Print the sorted data
        }
    }

    // Method to create and run sorting threads
    private static void createAndRunSortingThreads(List<String[]> data) {
        String[] columns = {"ID", "Name", "Category", "Quantity", "Price"}; // Array of column names
        for (int i = 0; i < columns.length; i++) { // Iterate through each column
            final int colIndex = i; // Column index for sorting
            new Thread(() -> { // Create a new thread
                List<String[]> sortedData = new ArrayList<>(data); // Create a copy of the data
                sortData(sortedData, colIndex); // Sort the data based on column index
                saveToFile(sortedData, "sortedStock_C" + colIndex + ".csv"); // Save sorted data to file
            }).start(); // Start the thread
        }
    }

    // Method to sort data based on column index
    private static void sortData(List<String[]> data, int colIndex) {
        Comparator<String[]> comparator; // Comparator for sorting

        // Determine comparator based on column index
        switch (colIndex) {
            case 0:
                comparator = Comparator.comparingInt(o -> Integer.parseInt(o[0]));
                break;
            case 1:
                comparator = Comparator.comparing(o -> o[1]);
                break;
            case 2:
                comparator = Comparator.comparing(o -> o[2]);
                break;
            case 3:
                comparator = Comparator.comparingInt(o -> Integer.parseInt(o[3]));
                break;
            case 4:
                comparator = Comparator.comparingDouble(o -> Double.parseDouble(o[4]));
                break;
            default:
                throw new IllegalArgumentException("Invalid column index");
        }

        data.sort(comparator); // Sort the data
    }

    // Method to save sorted data to a file
    private static void saveToFile(List<String[]> data, String fileName) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) { // Open file for writing
            bw.write("ID,Name,Category,Quantity,Price"); // Write header
            bw.newLine(); // New line after header

            // Write each row of data
            for (String[] row : data) {
                row[4] = String.format("%.2f", Double.parseDouble(row[4])); // Ensure price has two decimal places
                bw.write(String.join(",", row)); // Join row elements with commas
                bw.newLine(); // New line after each row
            }
        } catch (IOException e) { // Handle IO exceptions
            e.printStackTrace(); // Print stack trace for debugging
        }
    }

    // Method to print data in a table format
    private static void printData(List<String[]> data) {
        // Print the data in a table format
        System.out.printf("%-5s %-20s %-15s %-10s %-10s%n", "ID", "Name", "Category", "Quantity", "Price"); // Print header
        System.out.println("---------------------------------------------------------------"); // Print separator
        for (String[] row : data) { // Iterate through each row
            System.out.printf("%-5s %-20s %-15s %-10s %-10s%n", row[0], row[1], row[2], row[3], String.format("%.2f", Double.parseDouble(row[4]))); // Print row with formatted price
        }
    }

    // Custom exception for invalid product name
    public static class InvalidProductNameException extends Exception {
        public InvalidProductNameException(String message) {
            super(message); // Call the constructor of Exception class with the message
        }
    }
}













