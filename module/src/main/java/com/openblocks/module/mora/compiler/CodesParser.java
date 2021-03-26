package com.openblocks.module.mora.compiler;

import com.openblocks.moduleinterface.models.OpenBlocksProjectMetadata;
import com.openblocks.moduleinterface.models.code.BlockCode;
import com.openblocks.moduleinterface.models.code.ParseBlockTask;
import com.openblocks.moduleinterface.models.layout.LayoutViewXMLAttribute;
import com.openblocks.moduleinterface.projectfiles.OpenBlocksCode;
import com.openblocks.moduleinterface.projectfiles.OpenBlocksLayout;

import java.util.HashMap;

public class CodesParser {
    public static String convertToJava(OpenBlocksCode code, OpenBlocksProjectMetadata metadata, HashMap<String, ParseBlockTask> blocks) {
        // This is the final code

        // NOTE: This version can only support one activity (MainActivity), for proof of concept
        // ANOTHER NOTE: This Mora compiler is unfinished, you can only add statements and stuff, you can't do imports, classes, etc, It's coming soon
        String converted_code =
                "package " + metadata.getPackageName() + ";\n" +
                "\n" +
                "import androidx.appcompat.app.AppCompatActivity;\n" +
                "\n" +
                "import android.widget.*;\n" + // This is just temporary
                "import android.os.Bundle;\n" +
                "\n" +
                "public class MainActivity extends AppCompatActivity {\n" +
                "\n" +
                "    @Override\n" +
                "    protected void onCreate(Bundle savedInstanceState) {\n" +
                "        super.onCreate(savedInstanceState);\n" +
                "        setContentView(R.layout.activity_main);\n";

        for (BlockCode block : code.blocks) {
            ParseBlockTask task = blocks.get(block.opcode);

            if (task == null) continue;

            task.parseBlock(converted_code);
        }

        converted_code +=
                "    }\n" +
                "}";

        return converted_code;
    }

    public static String convertToXML(OpenBlocksLayout layout) {
        String converted_xml =
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";

        converted_xml += convertToXML(layout, converted_xml, true);

        return converted_xml;
    }

    public static String convertToXML(OpenBlocksLayout layout, String converted_xml, boolean is_first) {
        StringBuilder converted_xml_builder = new StringBuilder(converted_xml);

        converted_xml_builder.append("<").append(layout.view_name).append("\n");

        if (is_first) {
            // First element should have android and app's xmlns
            converted_xml_builder.append(
                    "xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                    "xmlns:app=\"http://schemas.android.com/apk/res-auto\"\n");
        }

        // Serialize each xml attributes
        for (LayoutViewXMLAttribute xml_attribute : layout.xml_attributes) {
            // prefix:attribute_name="value"\n

            converted_xml_builder
                    .append(xml_attribute.prefix).append(":")
                    .append(xml_attribute.attribute_name).append("=\"")
                    .append(xml_attribute.value).append("\"\n");
        }

        if (layout.childs.isEmpty()) {
            converted_xml_builder.append("/>\n");
        } else {
            converted_xml_builder.append(">\n");

            for (OpenBlocksLayout child : layout.childs) {
                converted_xml_builder.append(
                        convertToXML(
                                child,
                                converted_xml_builder.toString(),
                                false
                        )
                );
            }

            converted_xml_builder.append("</").append(layout.view_name).append(">\n");
        }

        return converted_xml_builder.toString();
    }
}
