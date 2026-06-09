/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package au.gov.asd.tac.constellation.networkPlugin.uploadReportsPlugin;

import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import static au.gov.asd.tac.constellation.networkPlugin.uploadReportsPlugin.UploadReportsPluginUtilities.addFilesToRecord;
import static au.gov.asd.tac.constellation.networkPlugin.uploadReportsPlugin.UploadReportsPluginUtilities.processFileData;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.importexport.delimited.parser.ImportFileParser;
import au.gov.asd.tac.constellation.plugins.parameters.ParameterChange;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType.FileParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPluginCoreType;
import au.gov.asd.tac.constellation.views.dataaccess.templates.RecordStoreQueryPlugin;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * Upload Network Reports Plugin.
 */
@ServiceProviders({
    @ServiceProvider(service=DataAccessPlugin.class),
    @ServiceProvider(service=Plugin.class)
})
@NbBundle.Messages("UploadNetworkReportsPlugin=Upload Network Reports")
public class UploadNetworkReportsPlugin extends RecordStoreQueryPlugin implements DataAccessPlugin {
    
    public static final String FILE_TYPE_PARAMETER_ID = PluginParameter.buildId(UploadNetworkReportsPlugin.class, "file_type");
    public static final String FILE_TYPE_PARAMETER_LABEL = "File Type";
    public static final String FILE_TYPE_PARAMETER_DESCRIPTION = "The type of file to import";
    
    public static final String FILE_NAME_PARAMETER_ID = PluginParameter.buildId(UploadNetworkReportsPlugin.class, "file_name");
    public static final String FILE_NAME_PARAMETER_LABEL = "File";
    public static final String FILE_NAME_PARAMETER_DESCRIPTION = "File to extract graph from";
    
    private static final List<String> SUPPORTED_FILE_TYPES = new ArrayList<>(List.of("CSV", "Excel"));
    
    private static final Map<String, ExtensionFilter> FILTERS = new HashMap<>(Map.of(
            "CSV", new FileChooser.ExtensionFilter(
                    String.format("CSV files (%s)", FileExtensionConstants.COMMA_SEPARATED_VALUE), 
                    FileExtensionConstants.COMMA_SEPARATED_VALUE),
            "Excel", new FileChooser.ExtensionFilter(
                    String.format("Excel files (%s)", FileExtensionConstants.XLSX),
                    FileExtensionConstants.XLSX)
    ));
    
    private static final Map<String, ImportFileParser> PARSERS = ImportFileParser.getParsers().entrySet().stream()
            .filter(entry -> SUPPORTED_FILE_TYPES.contains(entry.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    
    @Override
    public String getType() {
        return DataAccessPluginCoreType.NETWORK_PLUGINS;
    }

    @Override
    public int getPosition() {
        return 1;
    }
    
    @Override
    public PluginParameters createParameters() {
        final PluginParameters params = new PluginParameters();
        
        final String defaultFileType = SUPPORTED_FILE_TYPES.get(0); // Default to CSVs
        
        final PluginParameter<SingleChoiceParameterType.SingleChoiceParameterValue> fileTypeParam = SingleChoiceParameterType.build(FILE_TYPE_PARAMETER_ID);
        fileTypeParam.setName(FILE_TYPE_PARAMETER_LABEL);
        fileTypeParam.setDescription(FILE_TYPE_PARAMETER_DESCRIPTION);
        SingleChoiceParameterType.setOptions(fileTypeParam, SUPPORTED_FILE_TYPES);
        SingleChoiceParameterType.setChoice(fileTypeParam, defaultFileType);
        params.addParameter(fileTypeParam);
        
        final PluginParameter<FileParameterValue> fileNameParam = FileParameterType.build(FILE_NAME_PARAMETER_ID);
        fileNameParam.setName(FILE_NAME_PARAMETER_LABEL);
        fileNameParam.setDescription(FILE_NAME_PARAMETER_DESCRIPTION);
        FileParameterType.setFileFilters(fileNameParam, FILTERS.get(defaultFileType));
        params.addParameter(fileNameParam);
        
        params.addController(FILE_TYPE_PARAMETER_ID, (master, parameters, change) -> {
            if (change == ParameterChange.VALUE) {
                final String selection = master.getStringValue();
                
                @SuppressWarnings("unchecked") //FILE_NAME_PARAMETER_ID is always of type FileParameter
                final PluginParameter<FileParameterValue> fileName = (PluginParameter<FileParameterValue>) parameters.get(FILE_NAME_PARAMETER_ID);
                // clear filename string on file type change
                if (!fileName.getStringValue().isEmpty()) {
                    fileName.getParameterValue().setStringValue("");
                    fileName.fireChangeEvent(ParameterChange.VALUE);
                }
                FileParameterType.setFileFilters(fileName, FILTERS.get(selection));
            }
        });
        
        return params;
    }
    
    @Override
    protected RecordStore query(RecordStore query, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException {
        final String fileType = parameters.getParameters().get(FILE_TYPE_PARAMETER_ID).getStringValue();
        final List<File> files = (List<File>) parameters.getParameters().get(FILE_NAME_PARAMETER_ID).getObjectValue();
        
        final ImportFileParser parser = PARSERS.get(fileType);
        
        final List<List<String[]>> validFiles = new ArrayList<>();
        final StringBuilder errorMessages = new StringBuilder();
        
        for (File file : files) {
            try {
                final List<String[]> processedData = processFileData(file, parser);
                validFiles.add(processedData);
            } catch (MissingHeadersException e) {
                errorMessages.append(e.getMessage());
            } catch (IOException e) {
                errorMessages.append(file.getPath()).append(" could not be parsed.");
            }
        }
        
        if (!errorMessages.isEmpty()) {
            throw new PluginException(PluginNotificationLevel.ERROR, errorMessages.toString());
        }
        
        final RecordStore result = new GraphRecordStore();
        addFilesToRecord(validFiles, result);

        return result;
    }
    
}
