package views;

import Model.data;
import dao.DataDAO;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class UserView {
    private  String email;
    UserView(String email){
        this.email=email;
    }
    public void home(){
        System.out.println("Welcome"+this.email);
        do{
            System.out.println("1:Show hidden files.");
            System.out.println("2:Hide new files.");
            System.out.println("3.UnHide a file");
            System.out.println("4.Open a hidden file");
            System.out.println("0:Exit");
            Scanner sc = new Scanner(System.in);
            int ch = Integer.parseInt(sc.nextLine());

            switch (ch){
                case 1 ->{
                    try {
                        List<data> files = DataDAO.getALLFiles(this.email);
                        System.out.println("ID - File Name");
                        for(data file:files){
                            System.out.println(file.getId()+" - "+file.getFileName());
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
                case 2 -> {
                    System.out.println("Enter the file path:");
                    String path = sc.nextLine();
                    File f = new File(path);
                    data file = new data(0,f.getName(),path,this.email);
                    try {
                        DataDAO.hideFile(file);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                case 3 -> {
                    List<data> files = null;
                    try {
                        files = DataDAO.getALLFiles(this.email);
                    System.out.println("ID - File Name");
                    for(data file:files){
                        System.out.println(file.getId()+ " - "+file.getFileName());
                    }
                    System.out.println("Enter the id of file to unhide:");
                    int id = Integer.parseInt(sc.nextLine());

                    boolean isValidID = false;

                    for(data file:files){
                        if(file.getId()==id){
                            isValidID = true;
                            break;
                        }
                    }
                    if(isValidID){
                        DataDAO.unhide(id);
                    }else {
                        System.out.println("Wrong ID");
                    }
                    } catch (SQLException | IOException e) {
                        throw new RuntimeException(e);
                    }

                }
                case 4 -> {
                    List<data> files = null;
                    try {
                        files = DataDAO.getALLFiles(this.email);
                        System.out.println("ID - File Name");
                        for(data file:files){
                            System.out.println(file.getId()+ " - "+file.getFileName());
                        }
                        System.out.println("Enter the id of file to open:");
                        int id = Integer.parseInt(sc.nextLine());

                        boolean isValidID = false;
                        for(data file:files){
                            if(file.getId() == id){
                                isValidID = true;
                                break;
                            }
                        }

                        if(isValidID){
                            DataDAO.openHiddenFile(id);   // <-- call the function we wrote
                        } else {
                            System.out.println("Wrong ID");
                        }
                    } catch (SQLException | IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                case 0 -> {
                    System.exit(0);
                }
            }
        }while (true);
    }
}
