package Linkedin;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;

import static java.lang.Thread.sleep;

public class LinkedInPreProcessing {

    static final int currentYear = 2021;

    protected static String addAttributeToString(String varName, ArrayList<String> attribute) {
        return varName + ":" + attribute.toString().replace(", ", ",") + "\t";
    }

    public static void main(String args[]) throws FileNotFoundException {
        try {
//            Scanner dbScanner = new Scanner(new File(System.getProperty("user.dir") + "/LinkedIn/textFiles/linkedin.json"));

            BufferedReader dbScanner = new BufferedReader(new FileReader(System.getProperty("user.dir") + "/LinkedIn/textFiles/linkedin.json"));
            int nodeIndex = 0;

            ArrayList<String> data = new ArrayList<>();
            String line;

            while ((line = dbScanner.readLine()) != null && nodeIndex < 2900000) {

//            while (dbScanner.hasNext() && nodeIndex < 200000) {
//                String line = dbScanner.nextLine();
                String id;
                if (line.length() >= 10 && line.contains("_id") && line.indexOf(",") != -1) {
                    id = line.substring(11, line.indexOf(",") - 1);
                } else {
                    continue;
                }
//                    if (line.substring(10, line.indexOf(",")).equals("\"in-2jcwebb\"")) {
//                        System.out.println("Here");
//                    }
                byte[] bytes = line.getBytes(StandardCharsets.UTF_8);

                String utf8EncodedString = new String(bytes, StandardCharsets.UTF_8);
//                    System.out.println(utf8EncodedString);
//                    System.out.println(id);
                JSONObject obj;
                try {
                    obj = (JSONObject) new JSONParser().parse(utf8EncodedString);
                } catch (ParseException e) {
                    System.out.println("An error occurred.");
                    e.printStackTrace();
                    continue;
                }

                HashMap<String, ArrayList> attr = new HashMap();

                StringBuilder outputString = new StringBuilder();

                outputString.append(nodeIndex + "\t" +  id + "\t");

                if (obj.containsKey("name")) {
                    JSONObject nameObj = (JSONObject) obj.get("name");
                    ArrayList<String> name = new ArrayList<>();
                    name.add((String) nameObj.get("given_name"));
                    name.add((String) nameObj.get("family_name"));
                    attr.put("name", name);
                    outputString.append(addAttributeToString("name", name));
                }

//                    ArrayList<String> industry = new ArrayList<>();
                if (obj.containsKey("industry")) {
                    ArrayList<String> industry = new ArrayList<>();
                    if (obj.get("industry") instanceof String) {
                        industry.add((String) obj.get("industry"));
//                            System.out.println(obj.get("industry").toString());
//                            System.out.println("Here");
                    } else {
                        industry = (ArrayList) obj.get("industry");
                    }
                    attr.put("industry", industry);
//                        outputString.append("industry:" + industry.toString().replace(", ", ",") + "\t");
                    outputString.append(addAttributeToString("industry", industry));
                }

                if (obj.containsKey("skills")) {
                    ArrayList<String> skills = (ArrayList) obj.get("skills");
                    attr.put("skills", skills);
                    outputString.append(addAttributeToString("skills", skills));
//                        outputString.append("skills:" + skills.toString().replace(", ", ",") + "\t");
                }
//                        JSONObject obj = (JSONObject) new JSONParser().parse(dbScanner.nextLine());
//                        System.out.println(obj.get("_id").toString());
                if (obj.containsKey("education")) {
                    JSONArray education = (JSONArray) obj.get("education");
                    ArrayList<String> degree = new ArrayList<>();
                    ArrayList<String> major = new ArrayList<>();

                    for (int i = 0; i < education.size(); i++) {
                        JSONObject jsonobject = (JSONObject) education.get(i);
                        if (jsonobject.containsKey("degree")) {

                            if (jsonobject.get("degree") instanceof String) {
                                String degString = (String) jsonobject.get("degree");
                                degree.add(degString);
                            }
                            else {
                                degree = (ArrayList) jsonobject.get("degree");
                            }


//                                degrees.add(((String) jsonobject.get("degree")));
//                            degree = (String) jsonobject.get("degree");
                        }
                        if (jsonobject.containsKey("major")) {


                            if (jsonobject.get("major") instanceof String) {
                                String majString = (String) jsonobject.get("major");
                                major.add(majString);
                            }
                            else {
                                major = (ArrayList) jsonobject.get("major");
                            }

//                            ArrayList<String> major = (ArrayList) obj.get("major");


//                                majors.add(((String) jsonobject.get("major")));
//                            major = (String) jsonobject.get("major");
                        }
                    }

                    if (!Objects.isNull(degree)) {
                        attr.put("degree", degree);
                        outputString.append(addAttributeToString("degree", degree));
//                                    outputString.append("degree:" + degree.toString().replace(", ", ",") + "\t");
                    }
                    if (!Objects.isNull(major)) {
                        attr.put("major", major);
                        outputString.append(addAttributeToString("major", major));
//                                    outputString.append("major" + major.toString().replace(", ", ",") + "\t");
                    }

//                        System.out.println(node.getNodeIndex() + ": " + node.getAttributes());
                }

                if (obj.containsKey("experience")) {
                    JSONArray experience = (JSONArray) obj.get("experience");

                    int numberOfExperiences = experience.size();

                    ArrayList<String> title = new ArrayList<>();
                    int yearsExperience = 0;

                    for (int i = 0; i < experience.size(); i++) {
                        JSONObject jsonobject = (JSONObject) experience.get(i);
                        if (jsonobject.containsKey("title")) {
                            title.add((String) jsonobject.get("title"));
                        }
                        int experienceStart;
                        int experienceEnd;
                        if (jsonobject.containsKey("start") && jsonobject.containsKey("end")) {
                            String experienceStartString = (String) jsonobject.get("start");
                            String experienceEndString = (String) jsonobject.get("end");

                            if (experienceStartString.length() > 4 && experienceEndString.length() > 4) {

                                String lastCharsStart = experienceStartString.substring(experienceStartString.length() - 4);
                                String lastCharsEnd = experienceEndString.substring(experienceEndString.length() - 4);

                                if (lastCharsStart.chars()
                                        .allMatch(Character::isDigit)) {
                                    experienceStart = Integer.parseInt(lastCharsStart);

                                    if (lastCharsEnd.chars()
                                            .allMatch(Character::isDigit)) {
                                        experienceEnd = Integer.parseInt(lastCharsEnd);

                                        yearsExperience += experienceEnd - experienceStart;
                                    }
                                    else if (experienceEndString.contains("Present")) {
                                        yearsExperience += currentYear - experienceStart;
                                    }
                                }
                            }
                        }

//                                if (!Objects.isNull(major)) {
//                                    attr.put("major", major);
//                                    outputString.append(addAttributeToString("major", major));
////                                    outputString.append("major" + major.toString().replace(", ", ",") + "\t");
//                                }
                    }
                    if (yearsExperience != 0) {
                        ArrayList<String> years = new ArrayList<>();
                        years.add(String.valueOf(yearsExperience));
                        attr.put("yearsExperience", years);
                        outputString.append(addAttributeToString("yearsExperience", years));
                    }

                    ArrayList<String> numExperiences = new ArrayList<>();
                    numExperiences.add(String.valueOf(numberOfExperiences));
                    attr.put("numberOfExperiences", numExperiences);
                    outputString.append(addAttributeToString("numberOfExperiences", numExperiences));

                    attr.put("title", title);
                    outputString.append(addAttributeToString("title", title));
                }
                if (obj.containsKey("also_view")) {
                    JSONArray alsoView = (JSONArray) obj.get("also_view");
                    ArrayList<String> alsoViewID = new ArrayList<>();
                    for (int i = 0; i < alsoView.size(); i++) {
                        JSONObject view = (JSONObject) alsoView.get(i);
                        String getViewedID = (String) view.get("id");
                        alsoViewID.add(getViewedID);
                    }
                    attr.put("viewed", alsoViewID);
                    outputString.append(addAttributeToString("viewed", alsoViewID));
                }

//                LinkedInNode node = new LinkedInNode(nodeIndex, id, attr);
                data.add(outputString.toString());
//                    System.out.println(line);
//                    System.out.println(outputString.toString());
                nodeIndex += 1;

                System.out.println(nodeIndex);

            }

//                            node.addAttribute(obj.get("_id"), );
//                        jsonArray.add(obj);

            dbScanner.close();

//            FileWriter writer = new FileWriter("output_with_name.txt");
//            for(String str: data) {
//                writer.write(str + System.lineSeparator());
//            }
//            writer.close();

            FileWriter writer = new FileWriter("test.txt");
            for(String str: data) {
                writer.write(str + System.lineSeparator());
            }
            writer.close();

        }
        catch (IOException e) {
            System.out.println("An error occurred.");

            e.printStackTrace();
        }
    }
}
