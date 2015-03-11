/**
 * @created 2015-02-01
 * @author gideon mw jones.
 */

package uk.ac.aber.gij2.olandroid;

import android.content.Context;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import uk.ac.aber.gij2.olandroid.visualisation.Component;
import uk.ac.aber.gij2.olandroid.visualisation.Manoeuvre;


public class ManoeuvreCatalogue {

   // we want a predictable iteration order
   private LinkedHashMap<String, Manoeuvre> catalogue;
   private String[] categories;
   private Context context;


   /**
    * @param context - the context relevant for getting the xml
    */
   public ManoeuvreCatalogue(Context context) {

      this.context = context;
      catalogue = new LinkedHashMap<>();

      try {
         parseCategories();

      } catch (XmlPullParserException | IOException exception) {
         Log.d(this.getClass().getName(), exception.getMessage());
      }
   }


   /**
    * parses the xml manoeuvre catalogue's categories into the catalogue
    * @throws XmlPullParserException
    * @throws IOException
    */
   protected void parseCategories() throws XmlPullParserException, IOException {

      ArrayList<String> categoriesTemp = new ArrayList<>();

      XmlPullParser parser = context.getResources().getXml(R.xml.manoeurvre_catalogue);

      // skipping over the first two element - xml declarations & top level container.
      parser.next();
      parser.next();

      parser.require(XmlPullParser.START_TAG, null, "catalogue");

      while (parser.next() != XmlPullParser.END_TAG) {
         if (parser.getName().equals("category")) {

            String category = parser.getAttributeValue(null, "name");

            boolean inList = false;
            for (int i = 0; i < categoriesTemp.size() && !inList; i++) {
               if (categoriesTemp.get(i).equals(category)) {
                  inList = true;
               }
            }

            if (!inList) {
               categoriesTemp.add(category);
            }

            parseManoeuvres(parser, category);
         }
      }

      categories = categoriesTemp.toArray(new String[categoriesTemp.size()]);
   }


   /**
    * parses the xml manoeuvre catalogue category's manoeuvres into the catalogue
    * @throws XmlPullParserException
    * @throws IOException
    */
   protected void parseManoeuvres(XmlPullParser parser, String category) throws
      XmlPullParserException, IOException {

      parser.require(XmlPullParser.START_TAG, null, "category");

      while (parser.next() != XmlPullParser.END_TAG) {
         if (parser.getName().equals("manoeuvre")) {

            parseManoeuvreVariants(parser, parser.getAttributeValue(null, "olan"), category);
         }
      }
   }




   /**
    * @param parser - the parsing application for the xml
    * @param olan - the olan name of the top manoeuvre
    * @throws XmlPullParserException
    * @throws IOException
    */
   protected void parseManoeuvreVariants(XmlPullParser parser, String olan, String category) throws
      XmlPullParserException, IOException {

      parser.require(XmlPullParser.START_TAG, null, "manoeuvre");

      while (parser.next() != XmlPullParser.END_TAG) {
         if (parser.getName().equals("variant")) {

            // have to get these now before the parser moves onto the components
            String fullOLAN = parser.getAttributeValue(null, "olanPrefix") + olan,
               name = parser.getAttributeValue(null, "name");

            try {
               catalogue.put(fullOLAN, new Manoeuvre(parseVariantComponents(parser), fullOLAN,
                  name, category));

            } catch (IndexOutOfBoundsException exception) {
               catalogue.remove(fullOLAN);
               Log.w(this.getClass().getName(), "invalid manoeuvre");
            }
         }
      }
   }


   /**
    * @param parser - the parsing application for the xml
    * @return - a list of components parsed from the xml
    * @throws XmlPullParserException
    * @throws IOException
    */
   protected Component[] parseVariantComponents(XmlPullParser parser) throws XmlPullParserException,
      IOException {

      ArrayList<Component> components = new ArrayList<>();

      parser.require(XmlPullParser.START_TAG, null, "variant");

      while (parser.next() != XmlPullParser.END_TAG) {
         if (parser.getName().equals("component")) {

            components.add(new Component(
               parseComponentStrength(parser.getAttributeValue(null, "pitch")),
               parseComponentStrength(parser.getAttributeValue(null, "yaw")),
               parseComponentStrength(parser.getAttributeValue(null, "roll")),
               Float.parseFloat(parser.getAttributeValue(null, "length")),
               ((OLANdroid) context.getApplicationContext()).getCurrentColourTheme(
                  R.array.colour_theme_front),
               ((OLANdroid) context.getApplicationContext()).getCurrentColourTheme(
                  R.array.colour_theme_back)));

            // skipping content
            skip(parser);
         }
      }

      return components.toArray(new Component[components.size()]);
   }


   /**
    * @param strength - the word to parse
    * @return - Component.MAX, Component.ZERO, Component.MIN
    */
   protected int parseComponentStrength(String strength) {

      switch (strength) {
         case "MAX":
            return Component.MAX;
         case "ZERO":
            return Component.ZERO;
         case "MIN":
            return Component.MIN;
      }
      return Component.ZERO;
   }


   /**
    * @param parser - the parsing application for the xml
    * @throws XmlPullParserException
    * @throws IOException
    */
   protected void skip(XmlPullParser parser) throws XmlPullParserException, IOException {

      int depth = 1;
      while (depth != 0) {
         switch (parser.next()) {
            case XmlPullParser.END_TAG:
               depth--;
               break;
            case XmlPullParser.START_TAG:
               depth++;
               break;
         }
      }
   }


   /**
    * querying the manoeuvre catalogue with a key
    * @param key - the olan key to look for
    * @return - a manoeuvre with olan matching the input
    * @throws NullPointerException - might not be able to find the manoeuvre specified
    */
   public Manoeuvre get(String key) throws NullPointerException {
      return catalogue.get(key);
   }


   /**
    * querying the manoeuvre catalogue with a category
    * @param category - the category to look for
    * @return - an array of manoeuvres in that category
    * @throws NullPointerException - if the category isn't valid
    */
   public Manoeuvre[] getManoeuvres(String category) throws NullPointerException {
      ArrayList<String> ids = new ArrayList<>(catalogue.keySet());
      ArrayList<Manoeuvre> inCategory = new ArrayList<>();

     for (int i = 0; i < ids.size(); i++) {
         if (catalogue.get(ids.get(i)).getCategory().equals(category)) {
            inCategory.add(catalogue.get(ids.get(i)));
         }
      }

      return inCategory.toArray(new Manoeuvre[inCategory.size()]);
   }


   /**
    * @return - an array of categories of manoeuvres found in the catalogue
    */
   public String[] getCategories() {
      return categories;
   }
}