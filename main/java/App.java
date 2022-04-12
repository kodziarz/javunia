import com.fasterxml.uuid.Generators;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfWriter;
import spark.Request;
import spark.Response;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static spark.Spark.*;

public class App {
    public static int nextIndex = 1;
    public static ArrayList<Car> cars = new ArrayList<>();
    public static ArrayList<String> models = new ArrayList<>() {
        {
            add("Renault");
            add("Fiat");
            add("Opel");
            add("Pojazd ludowy");
        }
    };

    public static void main(String[] args) {

        staticFiles.location("/");
        post("/add", (req, res) -> handleAdd(req, res));
        post("/json", (req, res) -> handleJson(req, res));
        post("/delete", (req, res) -> handleDelete(req, res));
        post("/update", (req, res) -> handleUpdate(req, res));
        post("/costam", (req, res) -> handleDefaultGet(req, res));
        post("/generateRandom", (req, res) -> handleGenerateCars(req, res));
        post("/generateInvoice", (req, res) -> handleGenerateInvoice(req, res));

    }

    public static String handleDefaultGet(Request req, Response res) {

        return "test";
    }

    public static String handleAdd(Request req, Response res) {

        System.out.println("dane: " + req.body());
        Gson gson = new Gson();
        System.out.println();
        TmpCar tmpCar = gson.fromJson(req.body(), TmpCar.class);
        Car car = new Car(tmpCar);
//        car.generateUUID();
//        car.generateId();

        res.type("application/json");
        return gson.toJson(car, Car.class);
    }

    public static String handleJson(Request req, Response res) {

        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<Car>>() {
        }.getType();
        System.out.println("wysyłam po poście na /json: " + gson.toJson(cars, listType));
        return gson.toJson(cars, listType);
    }

    public static String handleDelete(Request req, Response res) {

        Gson gson = new Gson();
        Car carToRemove = gson.fromJson(req.body(), Car.class);

        App.cars.removeIf(car -> car.getId() == carToRemove.getId());

        System.out.println("usunięto");
        System.out.println("obecna lista aut: " + cars);

        //Gson gson = new Gson();
        //Type listType = new TypeToken<ArrayList<Car>>(){}.getType();
        //System.out.println("wysyłam po poście na /json: " + gson.toJson(cars, listType) );
        return "{\"usunieto\":\"tak\"}";
    }

    private static String handleUpdate(Request req, Response res) {
        Gson gson = new Gson();
        Car carToUpdate = gson.fromJson(req.body(), Car.class);

        for (Car car : App.cars) {
            if (carToUpdate.getId() == car.getId()) {
                int index = App.cars.indexOf(car);
                App.cars.set(index, carToUpdate);
            }
        }

        return "{\"wynik\":\"siadło\"}";
    }

    private static String handleGenerateCars(Request req, Response res) {

        System.out.println("przyszło zapytanie o losowanie samochodów");
        for (int i = 0; i < 10; i++) {
            Car car = new Car();
        }

        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<Car>>() {
        }.getType();
        System.out.println("wysyłam po poście na /json: " + gson.toJson(cars, listType));
        return gson.toJson(cars, listType);
    }

    public static String handleGenerateInvoice(Request req, Response res) {

        Gson gson = new Gson();
        Car carToGenerateInvoice = gson.fromJson(req.body(), Car.class);

        Document document = new Document(); // dokument pdf
        String path = "invoices/plik.pdf"; // lokalizacja zapisu
        try {
            PdfWriter.getInstance(document, new FileOutputStream(path));
        } catch (DocumentException | FileNotFoundException e) {
            e.printStackTrace();
        }

        document.open();
        Font font = FontFactory.getFont(FontFactory.COURIER, 16, BaseColor.BLACK);
        Chunk chunk = new Chunk("tekst", font); // akapit

        try {
            document.add(chunk);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        document.close();

        res.type("application/octet-stream"); //
        res.header("Content-Disposition", "attachment; filename=plik.pdf"); // nagłówek

        OutputStream outputStream = null;
        try {
            outputStream = res.raw().getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            outputStream.write(Files.readAllBytes(Path.of("invoices/plik.pdf"))); // response pliku do przeglądarki
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    private static class Car {
        private int id;
        private UUID uuid;
        private boolean wasInvoiceCreated;
        private String model;
        private int year;
        private ArrayList<Airbag> airbags;
        private String color;

        public Car() {
            // konstruktor losowy
            this.generateId();
            this.generateUUID();
            this.wasInvoiceCreated = false;

            Random rand = new Random();
            int index = rand.nextInt(models.size());
            this.model = models.get(index);

            this.year = rand.nextInt(80) + 1940;

            this.airbags = new ArrayList<>();

            this.airbags.add(new Airbag("kierowca", rand.nextBoolean()));
            this.airbags.add(new Airbag("pasażer", rand.nextBoolean()));
            this.airbags.add(new Airbag("tylna kanapa", rand.nextBoolean()));
            this.airbags.add(new Airbag("boczne z tyłu", rand.nextBoolean()));

            int color = rand.nextInt(256 * 256 * 256);
            this.color = "#" + Integer.toHexString(color);


            cars.add(this);
        }


        public Car(String model, int year, ArrayList<Airbag> airbags, String color) {

            this.wasInvoiceCreated = false;

            this.model = model;
            this.year = year;
            this.airbags = airbags;
            this.color = color;

            this.generateId();
            this.generateUUID();

            cars.add(this);
        }

        public Car(TmpCar tmpCar) {

            if(tmpCar.getId() != 0){
                this.id = tmpCar.getId();
            } else {
                this.generateId();
            }

            if(tmpCar.getUuid() != null){
                this.uuid = UUID.fromString(tmpCar.getUuid());
            } else {
                this.generateUUID();
            }
            this.wasInvoiceCreated = tmpCar.isWasInvoiceCreated();
            this.model = tmpCar.getModel();
            this.year = tmpCar.getYear();
            this.airbags = tmpCar.getAirbagsExistance();
//            HashMap<String, Boolean> airbags = tmpCar.getAirbagsExistance();
//            for (Map.Entry<String, Boolean> entry : airbags.entrySet()){
//                Airbag airbag = new Airbag(entry.getKey(), entry.getValue());
//                this.airbags.add(airbag);
//            }
            this.color = tmpCar.getColor();
            cars.add(this);
        }

        public int getId() {
            return id;
        }

        public UUID getUuid() {
            return uuid;
        }

        public String getModel() {
            return model;
        }

        public int getYear() {
            return year;
        }

        public ArrayList<Airbag> getAirbags() {
            return airbags;
        }

        public String getColor() {
            return color;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public void setAirbags(ArrayList<Airbag> airbags) {
            this.airbags = airbags;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public void generateUUID() {
            this.uuid = Generators.randomBasedGenerator().generate();
        }

        public void generateId() {
            this.id = nextIndex;
            App.nextIndex++;
        }
    }

    private static class TmpCar {
        private int id;
        private String model;
        private int year;
        private String uuid;
        private boolean wasInvoiceCreated;
        //private String[] airbagsNames;
        private ArrayList<Airbag> airbagsExistance;
        private String color;

        public String getUuid() {
            return uuid;
        }

        public int getId() {
            return id;
        }

        public boolean isWasInvoiceCreated() {
            return wasInvoiceCreated;
        }

        public String getModel() {
            return model;
        }

        public int getYear() {
            return year;
        }

//        public String[] getAirbagsNames() {
//            return airbagsNames;
//        }


        public ArrayList<Airbag> getAirbagsExistance() {
            return airbagsExistance;
        }

        public String getColor() {
            return color;
        }
    }

    private static class Airbag {
        String name;
        boolean exists;

        public Airbag(String name, boolean exists) {
            this.name = name;
            this.exists = exists;
        }

        public String getName() {
            return name;
        }

        public boolean isExists() {
            return exists;
        }
    }
}
