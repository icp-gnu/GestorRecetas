package GestorRecetas;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GestorFicheros {

    private static final String FICHERO_DAT = "recetas.dat";
    private static final String FICHERO_XML = "recetas.xml";
    private static final String FICHERO_JSON = "configuracion.json";

    private static final Pattern PATRON_ULTIMA_OPCION = Pattern.compile("\"ultimaOpcion\"\\s*:\\s*(\\d+)");
    private static final Pattern PATRON_TOTAL_RECETAS = Pattern.compile("\"totalRecetas\"\\s*:\\s*(\\d+)");

    public static void verificarEInicializarCatalogo() {
        File file = new File(FICHERO_DAT);
        if (!file.exists()) {
            System.out.println("[INFO] No se encontró 'recetas.dat'. Creando catálogo inicial...");
            List<Receta> catalogoInicial = new ArrayList<>();
            catalogoInicial.add(new Receta(1, "Tortilla de Patatas", "Plato Principal", 35, "Fácil",
                    Arrays.asList("Patatas", "Huevos", "Cebolla", "Aceite de Oliva", "Sal")));
            catalogoInicial.add(new Receta(2, "Paella Valenciana", "Plato Principal", 60, "Media",
                    Arrays.asList("Arroz", "Pollo", "Conejo", "Judías Verdes", "Garrofón", "Azafrán")));
            catalogoInicial.add(new Receta(3, "Tarta de Queso", "Postre", 45, "Fácil",
                    Arrays.asList("Queso Crema", "Nata", "Huevos", "Azúcar", "Galletas", "Mantequilla")));
            guardarRecetasBinario(catalogoInicial);
        }
    }

    public static boolean guardarRecetasBinario(List<Receta> recetas) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FICHERO_DAT))) {
            oos.writeObject(recetas);
            return true;
        } catch (IOException e) {
            System.err.println("[ERROR] No se pudo guardar en " + FICHERO_DAT + ": " + e.getMessage());
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Receta> cargarRecetasBinario() {
        File file = new File(FICHERO_DAT);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return new ArrayList<>((List<Receta>) ois.readObject());
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            System.err.println("[ERROR] Error al leer " + FICHERO_DAT + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static boolean exportarAXML(List<Receta> recetas) {
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = db.newDocument();

            Element root = doc.createElement("recetas");
            doc.appendChild(root);

            for (Receta r : recetas) {
                Element elemReceta = doc.createElement("receta");
                elemReceta.setAttribute("id", String.valueOf(r.getId()));

                elemReceta.appendChild(crearElemento(doc, "nombre", r.getNombre()));
                elemReceta.appendChild(crearElemento(doc, "categoria", r.getCategoria()));
                elemReceta.appendChild(crearElemento(doc, "tiempoPreparacion",
                        String.valueOf(r.getTiempoPreparacion())));
                elemReceta.appendChild(crearElemento(doc, "dificultad", r.getDificultad()));

                Element ingredientes = doc.createElement("ingredientes");
                for (String ing : r.getIngredientes()) {
                    ingredientes.appendChild(crearElemento(doc, "ingrediente", ing));
                }
                elemReceta.appendChild(ingredientes);

                root.appendChild(elemReceta);
            }

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform(new DOMSource(doc), new StreamResult(new File(FICHERO_XML)));

            System.out.println("[OK] Catálogo exportado correctamente a '" + FICHERO_XML + "'.");
            return true;
        } catch (Exception e) {
            System.err.println("[ERROR] Fallo al exportar a XML: " + e.getMessage());
            return false;
        }
    }

    private static Element crearElemento(Document doc, String nombre, String texto) {
        Element e = doc.createElement(nombre);
        e.setTextContent(texto);
        return e;
    }

    public static boolean guardarConfiguracion(Configuracion config) {
        String json = "{\n"
                + "  \"ultimaOpcion\": " + config.getUltimaOpcion() + ",\n"
                + "  \"totalRecetas\": " + config.getTotalRecetas() + "\n"
                + "}\n";
        try {
            Files.write(new File(FICHERO_JSON).toPath(), json.getBytes(StandardCharsets.UTF_8));
            System.out.println("[OK] Configuración guardada en '" + FICHERO_JSON + "'.");
            return true;
        } catch (IOException e) {
            System.err.println("[ERROR] No se pudo guardar la configuración JSON: " + e.getMessage());
            return false;
        }
    }

    public static Configuracion cargarConfiguracion() {
        File file = new File(FICHERO_JSON);
        if (!file.exists()) {
            return new Configuracion();
        }
        try {
            String contenido = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            int ultimaOpcion = extraerEntero(PATRON_ULTIMA_OPCION, contenido);
            int totalRecetas = extraerEntero(PATRON_TOTAL_RECETAS, contenido);
            return new Configuracion(ultimaOpcion, totalRecetas);
        } catch (IOException e) {
            System.err.println("[ERROR] Fallo al leer configuración JSON: " + e.getMessage());
            return new Configuracion();
        }
    }

    private static int extraerEntero(Pattern patron, String texto) {
        Matcher m = patron.matcher(texto);
        return m.find() ? Integer.parseInt(m.group(1)) : 0;
    }
}
