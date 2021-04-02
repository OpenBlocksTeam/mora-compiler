package com.openblocks.module.mora.compiler;

import android.content.Context;
import android.os.FileUtils;

import com.android.apksigner.ApkSignerTool;
import com.openblocks.moduleinterface.OpenBlocksModule;
import com.openblocks.moduleinterface.callbacks.Logger;
import com.openblocks.moduleinterface.exceptions.CompileException;
import com.openblocks.moduleinterface.models.OpenBlocksProjectMetadata;
import com.openblocks.moduleinterface.models.code.ParseBlockTask;
import com.openblocks.moduleinterface.models.compiler.IncludedBinary;
import com.openblocks.moduleinterface.models.config.OpenBlocksConfig;
import com.openblocks.moduleinterface.projectfiles.OpenBlocksCode;
import com.openblocks.moduleinterface.projectfiles.OpenBlocksLayout;

import org.eclipse.jdt.core.compiler.CompilationProgress;
import org.eclipse.jdt.core.compiler.batch.BatchCompiler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MoraCompiler implements OpenBlocksModule.ProjectCompiler {

    Logger l;
    WeakReference<Context> context;

    @Override
    public Type getType() {
        return Type.PROJECT_COMPILER;
    }

    @Override
    public void initialize(Context context, Logger logger) {
        this.l = logger;
        this.context = new WeakReference<>(context);
        l.trace(this.getClass(), "Initialize");
    }

    @Override
    public OpenBlocksConfig setupConfig() {
        return null;
    }

    @Override
    public void applyConfig(OpenBlocksConfig config) {

    }

    List<IncludedBinary> includedBinaries;
    HashMap<String, ParseBlockTask> blocks;

    @Override
    public void initializeCompiler(ArrayList<IncludedBinary> includedBinaries, HashMap<String, ParseBlockTask> blocksCollection) {
        this.includedBinaries = includedBinaries;
        this.blocks = blocksCollection;
    }

    @Override
    public void compile(OpenBlocksProjectMetadata metadata, OpenBlocksCode code, OpenBlocksLayout layout, String location) throws CompileException {

        // Initialize the cache_folder

        File cache_folder = new File(context.get().getFilesDir(), "compile_cache");

        if (cache_folder.exists()) {
            if (!cache_folder.delete()) {
                l.fatal(this.getClass(), "Failed to delete home/compile_cache");

                throw new CompileException("Error deleting home/compile_cache", "An unknown IO error occurred whilst trying to delete home/compile_cache");
            }
        }

        if (!cache_folder.mkdir()) {
            l.fatal(this.getClass(), "Failed to mkdir home/compile_cache");

            throw new CompileException("Error mkdir-ing compile_cache", "An unknown IO error occurred whilst trying to mkdir home/compile_cache");
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Get the necessary binaries                                                             //
        ////////////////////////////////////////////////////////////////////////////////////////////

        IncludedBinary aapt2 = null;
        IncludedBinary zipalign = null;

        for (IncludedBinary includedBinary : includedBinaries) {
            if (includedBinary.name.equals("aapt2")) {
                aapt2 = includedBinary;
            } else if (includedBinary.name.equals("zipalign")) {
                zipalign = includedBinary;
            }
        }

        if (aapt2 == null)
            throw new CompileException("aapt2 not found", "aapt2 binary is not found / included in the core app");

        if (zipalign == null)
            throw new CompileException("zipalign not found", "zipalign binary is not found / included in the core app");

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Start Compiling                                                                        //
        ////////////////////////////////////////////////////////////////////////////////////////////

        // We're going to do this on a different thread

        Thread compile_thread = new Thread(() -> {

            // First, let's generate the codes
            // Java code
            String java_code = CodesParser.convertToJava(code, metadata, blocks);

            // XML code
            String xml_code = CodesParser.convertToXML(layout);

            // Manifest file
            String manifest = CodesParser.generateManifest(metadata);

            // Save them
            try {
                FileUtil.writeFile(new File(cache_folder, "java_src/MainActivity.java"), java_code.getBytes());
                FileUtil.writeFile(new File(cache_folder, "res/layout/activity_main.xml"), xml_code.getBytes());
                FileUtil.writeFile(new File(cache_folder, "AndroidManifest.xml"), manifest.getBytes());

            } catch (IOException e) {
                e.printStackTrace();

                l.fatal(this.getClass(), "An error occurred whilst trying to save codes: " + e.getMessage());
                return;
            }

            // TODO: 4/2/21 Finish this 

            BatchCompiler.compile(new File(cache_folder, "java_src/MainActivity.java").getAbsolutePath(), null, null, new CompilationProgress() {
                @Override
                public void begin(int remainingWork) {

                }

                @Override
                public void done() {

                }

                @Override
                public boolean isCanceled() {
                    return false;
                }

                @Override
                public void setTaskName(String name) {

                }

                @Override
                public void worked(int workIncrement, int remainingWork) {

                }
            });
        });
    }

    private static class FileUtil {
        public static void writeFile(File file, byte[] data) throws IOException {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(data);
            outputStream.flush();
            outputStream.close();
        }

        public static byte[] readFile(File file) throws IOException {
            StringBuilder output = new StringBuilder();
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream result = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];

            int length;
            while ((length = fis.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }

            return result.toByteArray();
        }
    }
}
