package com.openblocks.module.mora.compiler;

import android.content.Context;

import com.openblocks.moduleinterface.OpenBlocksModule;
import com.openblocks.moduleinterface.callbacks.Logger;
import com.openblocks.moduleinterface.exceptions.CompileException;
import com.openblocks.moduleinterface.models.OpenBlocksProjectMetadata;
import com.openblocks.moduleinterface.models.compiler.IncludedBinary;
import com.openblocks.moduleinterface.models.config.OpenBlocksConfig;
import com.openblocks.moduleinterface.projectfiles.OpenBlocksCode;
import com.openblocks.moduleinterface.projectfiles.OpenBlocksLayout;

import java.util.ArrayList;

public class MoraCompiler implements OpenBlocksModule.ProjectCompiler {

    Logger l;

    @Override
    public Type getType() {
        return Type.PROJECT_COMPILER;
    }

    @Override
    public void initialize(Context context, Logger logger) {
        this.l = logger;
        l.trace(this.getClass(), "Initialize");
    }

    @Override
    public OpenBlocksConfig setupConfig() {
        return null;
    }

    @Override
    public void applyConfig(OpenBlocksConfig config) {

    }

    @Override
    public void initializeCompiler(ArrayList<IncludedBinary> includedBinaries) {

    }

    @Override
    public void compile(OpenBlocksProjectMetadata metadata, OpenBlocksCode code, OpenBlocksLayout layout, String location) throws CompileException {

    }
}
