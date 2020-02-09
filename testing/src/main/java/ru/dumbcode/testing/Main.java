package ru.dumbcode.testing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main 
{
    private static Logger mainLogger = LoggerFactory.getLogger(Main.class);
    private static Logger stackLogger = LoggerFactory.getLogger("ru.dumbcode.Stacktrace");

    private static String[] allMarks = {"mark01","mark17","mark23","mark35","markFV","markFX","markFT"};
    
    public static void main( String[] args )
    {
        Map<String, ArrayList<Integer>> CSVList = new TreeMap<String, ArrayList<Integer>>(String.CASE_INSENSITIVE_ORDER);
        for(String mark : allMarks) {
            CSVList.put(mark, null);
        }
        
        if(args.length == 0) {
            File defaultZip = new File("source_archive.zip");
            if(defaultZip.exists()) {
                zipParse(defaultZip.getAbsolutePath(), CSVList);
            }else {
                mainLogger.warn("Данные не найдены. Укажите путь вручную в аргументах");
                return;
            }
        }else {
            for(String arg : args) {
                File file = new File(arg);
                if(!file.exists()) {
                    mainLogger.warn("Аргумент \"{}\" неверен. Папка или файл не существует. Проигнорировано", arg);
                }else {
                    if(file.isDirectory()) {
                        folderParse(arg, CSVList);
                    }else if(arg.toLowerCase().endsWith(".zip")){
                        zipParse(arg, CSVList);
                    }else if(arg.toLowerCase().endsWith(".csv")) {
                        mainLogger.info("Обрабатываю файл: \"{}\"", file.getAbsolutePath());
                        try(InputStream stream = new FileInputStream(file)){
                            CSVParse(stream, CSVList);
                        } catch (Exception e) {
                            mainLogger.error("Ошибка при чтении файла \"{}\"", arg);
                            stackLogger.error("Ошибка при чтении файла \"{}\"", arg, e);
                        }
                        System.out.println();
                    }else {
                        mainLogger.warn("Файл \"{}\" имеет неверный тип(принимаются zip архивы и CSV файлы). Проигнорировано", arg);
                    }
                }
            }
        }
        
        //Проверяем, что список не пуст
        if(CSVList.values().stream().allMatch(Objects::isNull)) {
            mainLogger.warn("Результат пуст. Проверьте, что файлы данные для обработки не были пустыми");
            return;
        }
        
        //Суммируем отдельные элементы из списка вместе
        Map<String, Integer> marksValues = sumMarksValues(CSVList);
        
        //Выводим результаты в консоль и записываем в файлы
        JSONObject jsonResult1 = new JSONObject(marksValues);
        mainLogger.info("Первый результат: {}", jsonResult1.toString());
        writeJSONToFile(1, jsonResult1);
        
        JSONObject jsonResult2 = new JSONObject();
        for(String mark : allMarks) {
            jsonResult2.put(mark, marksValues.get(mark) == null ? JSONObject.NULL : marksValues.get(mark));
        }
        mainLogger.info("Второй результат: {}", jsonResult2.toString());
        writeJSONToFile(2, jsonResult2);

        JSONObject jsonResult3 = new JSONObject(CSVList);
        mainLogger.info("Третий результат: {}", jsonResult3.toString());
        writeJSONToFile(3, jsonResult3);
    }
    
    public static Map<String, Integer> sumMarksValues(Map<String, ArrayList<Integer>> CSVList){
        Map<String, Integer> outMap = new TreeMap<String, Integer>(String.CASE_INSENSITIVE_ORDER);
        
        CSVList.forEach((k, v) ->{
            if(v != null)
                outMap.put(k, v.stream().mapToInt(Integer::intValue).sum());
        });
        
        return outMap;
    }
    
    private static Map<String, ArrayList<Integer>> zipParse(String pathToZip,  Map<String, ArrayList<Integer>> CSVList) {
        mainLogger.info("Пытаюсь открыть архив \"{}\"", pathToZip);
        
        try(ZipFile zipFile = new ZipFile(pathToZip)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            
            mainLogger.info("Считываю архив...");
            System.out.println();
            
            while(entries.hasMoreElements()){
                ZipEntry entry = entries.nextElement();

                try(InputStream stream = zipFile.getInputStream(entry);) {
                    mainLogger.info("Обрабатываю файл: {}", entry.getName());
                    
                    CSVParse(stream, CSVList);
                }
                System.out.println();
            }
        } catch (IOException e) {
           mainLogger.error("Ошибка с архивом");
           stackLogger.error("Ошибка с архивом", e);
        }
        
        return CSVList;
    }
    
    private static Map<String, ArrayList<Integer>> folderParse(String pathToFolder, Map<String, ArrayList<Integer>> CSVList) {
        try (Stream<Path> walk = Files.walk(Paths.get(pathToFolder), 1)) {
            List<Path> files = walk.filter(x -> Files.isRegularFile(x) && x.getFileName().toString().toLowerCase().endsWith(".csv"))
                    .collect(Collectors.toList());
            for(Path file : files) {
                mainLogger.info("Обрабатываю файл: \"{}\"", file.toFile().getAbsolutePath());
                try(InputStream stream = new FileInputStream(file.toFile())){
                    CSVParse(stream, CSVList);
                }
                System.out.println();
            }
        }catch(IOException e) {
            mainLogger.error("Ошибка при обработке файлов");
            stackLogger.error("Ошибка при обработке файлов", e);
        }
        
        return CSVList;
    }
    
    public static Map<String, ArrayList<Integer>> CSVParse(InputStream CSVFile, Map<String, ArrayList<Integer>> outMap) {
        if(outMap == null)
            outMap = new TreeMap<String, ArrayList<Integer>>(String.CASE_INSENSITIVE_ORDER);
            
        try(CSVParser parser = CSVParser.parse(CSVFile, Charset.forName("UTF-8"), CSVFormat.DEFAULT.withCommentMarker('#'))){
            for(CSVRecord record : parser) {
                String markName = record.get(0);
                final int markVal = Integer.parseInt(record.get(1));
                mainLogger.info("Метка: {}, количество: {}", markName, markVal);
    
                addToList(outMap, markName, markVal);
            }
        }catch(Exception e) {
            mainLogger.error("Ошибка при обработке CSV файла");
            stackLogger.error("Ошибка при обработке CSV файла", e);
        }
        
        return outMap;
    }
    
    public static Map<String, ArrayList<Integer>> addToList(Map<String, ArrayList<Integer>> map, String mapKey, Integer mapValue) {
        ArrayList<Integer> itemsList = map.get(mapKey);

        //Создаём список, если его нет
        if(itemsList == null) {
             itemsList = new ArrayList<Integer>();
             itemsList.add(mapValue);
             map.put(mapKey, itemsList);
        } else {
           itemsList.add(mapValue);
        }
        
        //Сортируем список
        Collections.sort(itemsList);
        
        return map;
    }
    
    private static void writeJSONToFile(int type, JSONObject json) {
        File dir = new File("results");
        if (!dir.exists()) dir.mkdirs();
        
        File resultFile = new File(dir + File.separator + "result" + type + ".json");
        
        try(Writer writer = json.write(new FileWriter(resultFile))) {
            writer.flush();
            mainLogger.info("Результат #{} записан в файл \"{}\"", type, resultFile.getAbsolutePath());
            System.out.println();
        } catch (Exception e) {
            mainLogger.error("Не удалось записать результат в файл \"{}\"", resultFile.getPath());
            stackLogger.error("Не удалось записать результат в файл \"{}\"", resultFile.getPath(), e);
        }
    }
}
