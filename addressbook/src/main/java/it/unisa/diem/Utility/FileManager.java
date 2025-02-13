package it.unisa.diem.Utility;

import java.io.StreamCorruptedException;
import java.nio.file.Files;
import java.nio.file.Paths;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.io.text.VCardReader;
import ezvcard.io.text.VCardWriter;
import it.unisa.diem.Model.AddressBook;
import it.unisa.diem.Model.Contact;
import javafx.scene.image.Image;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class FileManager {

    private static final String profilePictureDir = "addressbook\\assets\\profile_pictures";
    private static final String profileListPath = "addressbook\\assets\\profile_list.obj";
    private static final String addressBookDir = "addressbook\\assets\\address_books";
    private static final String contactPictureDir = "addressbook\\assets\\contact_pictures";

    /**
     * Returns the path to the profile list file.
     * 
     * @return The path to the profile list file.
     */
    public static String getProfileListPath() {
        return profileListPath;
        
    }
    
    public static void createPaths(){
        try {
            Files.createDirectories(Paths.get(profilePictureDir));
            Files.createDirectories(Paths.get(addressBookDir));
            Files.createDirectories(Paths.get(contactPictureDir));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates the file path for an address book, based on the profile index.
     * The path will be in the format: "address_books/address_book_[timestamp].obj".
     * 
     * @param profileIndex The index of the profile.
     * @return The generated file path for the address book.
     */
    public static String generateAddressBookPath() {
        long timestamp = System.currentTimeMillis();
        return addressBookDir + "\\address_book_" + timestamp + ".obj";
    }
    
    /**
     * Generates a unique file path for a contact picture, using a timestamp for uniqueness.
     * The path will be in the format: "contact_pictures/contactPicture_[timestamp].[fileExtension]".
     * 
     * @param fileExtension The file extension (e.g., "jpg", "png") for the contact picture.
     * @return The generated file path for the contact picture.
     */
    public static String generateContactPicturePath(String fileExtension) {
        long timestamp = System.currentTimeMillis();
        return contactPictureDir + "\\contactPicture_" + timestamp + "." + fileExtension;
    }
    
    /**
     * Generates a unique file path for a profile picture, using a timestamp for uniqueness.
     * The path will be in the format: "profile_pictures/profilePicture_[timestamp].[fileExtension]".
     * 
     * @param fileExtension The file extension (e.g., "jpg", "png") for the profile picture.
     * @return The generated file path for the profile picture.
     */
    public static String generateProfilePicturePath(String fileExtension) {
        long timestamp = System.currentTimeMillis();
        return profilePictureDir + "\\profilePicture_" + timestamp + "." + fileExtension;
    }

    /**
     * Imports an object from a file.
     * 
     * @param path The file path to import the object from.
     * @param <T> The type of the object.
     * @return The deserialized object.
     * @throws IOException If the file stream is corrupted or not found.
     * @throws ClassCastException If the class of the object does not match.
     */
    @SuppressWarnings("unchecked")
    public static <T> T importFromFile(String path) throws StreamCorruptedException, ClassCastException, IOException {
        if(!Files.exists(Paths.get(path))){
            Files.createFile(Paths.get(path));
        }
        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(path)))) {
            return (T) ois.readObject();  // Deserialize the object
        }
         catch (IOException e) {
            throw new IOException("Failed to import from file: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new ClassCastException("Failed to import from file: " + e.getMessage());
        }
    }
    
    /**
     * Exports an object to a file.
     * 
     * @param path The file path to export the object to.
     * @param data The object to be written to the file.
     * @param <T> The type of the object.
     * @throws StreamCorruptedException If the file stream is corrupted.
     * @throws ClassCastException If the object type is incorrect.
     */
    public static <T> void exportToFile(String path, T data) throws StreamCorruptedException, ClassCastException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(data);  // Serialize the object
        } catch (IOException e) {
            throw new StreamCorruptedException("Failed to export to file: " + e.getMessage());
        }
    }

    /**
     * Imports an AddressBook from a file.
     * 
     * @param path The file path of the VCard to be imported.
     * @return The AddressBook to be imported.
     * @throws StreamCorruptedException If the file stream is corrupted.
     */
    public static AddressBook importFromVCard(String path) throws StreamCorruptedException {
        AddressBook addressBook = new AddressBook();
        try (FileInputStream fis = new FileInputStream(path); VCardReader reader = new VCardReader(fis)) {
            VCard vCard;
            while ((vCard = reader.readNext()) != null) {
                addressBook.add(
                    Contact.fromVCard(vCard));
            }
        } catch (IOException e) {
            throw new StreamCorruptedException("Failed to import from VCard: " + e.getMessage());
        }
        return addressBook;
    }

    /**
     * Exports an AddressBook to a VCard file.
     * 
     * @param path The file path to export the AddressBook as a VCard.
     * @param ab The AddressBook to be exported.
     * @throws StreamCorruptedException If the file stream is corrupted.
     */
    public static void exportAsVCard(String path, AddressBook ab) throws StreamCorruptedException, IOException {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }
        if(ab == null){
            throw new IllegalArgumentException("AddressBook cannot be null");
        }
        if(path.isEmpty()){
            throw new IllegalArgumentException("Path cannot be empty");
        }
        if(!path.endsWith(".vcf")){
            throw new IllegalArgumentException("Specified path is not a .vcf file");
        }
        if(!Files.exists(Paths.get(path))){
            try {
                Files.createFile(Paths.get(path));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (VCardWriter vCardWriter = new VCardWriter(new BufferedWriter(new FileWriter(path)), VCardVersion.V4_0)) {            
            // Iterate over AddressBook contacts and write them as VCards
            for (Contact contact : ab.contacts()) {
                // Write the vCard to the output stream
                vCardWriter.write(contact.toVCard());
            }
        // Close the writer automatically due to the try-with-resources
        }
    }
    
}