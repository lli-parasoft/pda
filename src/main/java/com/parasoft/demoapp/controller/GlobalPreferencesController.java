package com.parasoft.demoapp.controller;

import com.parasoft.demoapp.grpc.GRPCConfig;
import com.parasoft.demoapp.config.datasource.IndustryRoutingDataSource;
import com.parasoft.demoapp.dto.*;
import com.parasoft.demoapp.exception.*;
import com.parasoft.demoapp.model.global.preferences.GlobalPreferencesEntity;
import com.parasoft.demoapp.model.global.preferences.IndustryType;
import com.parasoft.demoapp.service.GlobalPreferencesDefaultSettingsService;
import com.parasoft.demoapp.service.GlobalPreferencesMQService;
import com.parasoft.demoapp.service.GlobalPreferencesService;
import com.parasoft.demoapp.service.ParasoftJDBCProxyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Tag(name = "demoAdmin")
@Controller
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class GlobalPreferencesController {

	@Autowired
	private GlobalPreferencesService globalPreferencesService;

	@Autowired
	private GlobalPreferencesDefaultSettingsService globalPreferencesDefaultSettingsService;

	@Autowired
	private ParasoftJDBCProxyService parasoftJDBCProxyService;

	@Autowired
	private GlobalPreferencesMQService globalPreferencesMQService;

	@Autowired
	private GRPCConfig gRPConfig;

	//@Hidden
	@Operation(description = "Update current preferences.")
	@ApiResponse(responseCode = "200", description = "Preferences have been updated.")
	@ApiResponse(responseCode = "400", description = "Invalid request parameter.",
				content = {@Content(schema = @Schema(hidden = true))})
	@ApiResponse(responseCode = "401",
				description = "You are not authorized to update current preferences.",
				content = {@Content(schema = @Schema(hidden = true))})
	@ApiResponse(responseCode = "404", description = "No preferences are currently configured and cannot be updated.",
				content = {@Content(schema = @Schema(hidden = true))})
	@PutMapping("/v1/demoAdmin/preferences")
	@ResponseBody
	public ResponseResult<GlobalPreferencesEntity> doSaveChanges(@RequestBody GlobalPreferencesDTO globalPreferencesDto)
			throws GlobalPreferencesNotFoundException, GlobalPreferencesMoreThanOneException, ParameterException,
			EndpointInvalidException, VirtualizeServerUrlException, MQServerIsNotAvailableException {

		IndustryType previousIndustry = IndustryRoutingDataSource.currentIndustry;

		ResponseResult<GlobalPreferencesEntity> response =
				ResponseResult.getInstance(ResponseResult.STATUS_OK, ResponseResult.MESSAGE_OK);
		response.setData(globalPreferencesService.updateGlobalPreferences(globalPreferencesDto));

		IndustryType currentIndustry = IndustryRoutingDataSource.currentIndustry;
		if(previousIndustry != currentIndustry){
			IndustryChangeMQMessageDTO messageDto = new IndustryChangeMQMessageDTO(previousIndustry, currentIndustry);
			globalPreferencesMQService.sendToIndustryChangeTopic(messageDto);
		}

		return response;
	}

	@Operation(description = "Obtain current industry")
	@ApiResponse(responseCode = "200", description = "Current industry was returned.")
	@ApiResponse(responseCode = "404", description = "No current industry.",
				content = {@Content(schema = @Schema(hidden = true))})
	@GetMapping("/v1/demoAdmin/currentIndustry")
	@ResponseBody
	public ResponseResult<IndustryType> getCurrentIndustry()
			throws GlobalPreferencesNotFoundException, GlobalPreferencesMoreThanOneException {

		ResponseResult<IndustryType> response =
				ResponseResult.getInstance(ResponseResult.STATUS_OK, ResponseResult.MESSAGE_OK);

		response.setData(globalPreferencesService.getCurrentIndustry());

		return response;
	}

	@Operation(description = "Obtain current preferences")
	@ApiResponse(responseCode = "200", description = "Current preferences was returned.")
	@ApiResponse(responseCode = "404", description = "No current preferences.",
				content = {@Content(schema = @Schema(hidden = true))})
	@GetMapping("/v1/demoAdmin/currentPreferences")
	@ResponseBody
	public ResponseResult<GlobalPreferencesResponseDTO> getCurrentPreferences()
			throws GlobalPreferencesNotFoundException, GlobalPreferencesMoreThanOneException {

		ResponseResult<GlobalPreferencesResponseDTO> response =
				ResponseResult.getInstance(ResponseResult.STATUS_OK, ResponseResult.MESSAGE_OK);
		GlobalPreferencesEntity globalPreferencesEntity = globalPreferencesService.getCurrentGlobalPreferences();
		GlobalPreferencesResponseDTO globalPreferencesResponseDTO = new GlobalPreferencesResponseDTO(globalPreferencesEntity);
		response.setData(globalPreferencesResponseDTO);

		return response;
	}

	@Operation(description = "Obtain default preferences")
	@ApiResponse(responseCode = "200", description = "Default preferences was returned.")
	@GetMapping("/v1/demoAdmin/defaultPreferences")
	@ResponseBody
	public ResponseResult<GlobalPreferencesResponseDTO> getDefaultPreferences() {

		ResponseResult<GlobalPreferencesResponseDTO> response =
				ResponseResult.getInstance(ResponseResult.STATUS_OK, ResponseResult.MESSAGE_OK);
		GlobalPreferencesEntity globalPreferencesEntity = globalPreferencesDefaultSettingsService.defaultPreferences();
		GlobalPreferencesResponseDTO globalPreferencesResponseDTO = new GlobalPreferencesResponseDTO(globalPreferencesEntity);
		response.setData(globalPreferencesResponseDTO);

		return response;
	}

	@Operation(description = "Reset databases for all industries.")
	@ApiResponse(responseCode = "200", description = "Databases for all industries has been reset.")
	@ApiResponse(responseCode = "401",
			description = "You are not authorized to reset databases for all industries.",
			content = {@Content(schema = @Schema(hidden = true))})
	@PutMapping("/v1/demoAdmin/databaseReset")
	@ResponseBody
	public ResponseResult<Void> resetAllIndustriesDatabase(){
		ResponseResult<Void> response =
				ResponseResult.getInstance(ResponseResult.STATUS_OK, ResponseResult.MESSAGE_OK);

		globalPreferencesService.resetAllIndustriesDatabase();

		return response;
	}

	@Operation(description = "Clear current industry database.")
	@ApiResponse(responseCode = "200", description = "Current industry database has been reset.")
	@ApiResponse(responseCode = "401",
			description = "You are not authorized to clear current industry database.",
			content = {@Content(schema = @Schema(hidden = true))})
	@PutMapping("/v1/demoAdmin/databaseClear")
	@ResponseBody
	public ResponseResult<Void> clearCurrentIndustryDatabase(){
		ResponseResult<Void> response =
				ResponseResult.getInstance(ResponseResult.STATUS_OK, ResponseResult.MESSAGE_OK);

		globalPreferencesService.clearCurrentIndustryDatabase();

		return response;
	}

	@Operation(description = "Validate Parasoft Virtualize server URL.")
	@ApiResponse(responseCode = "200", description = "Valid URL.")
	@ApiResponse(responseCode = "400", description = "Invalid URL.",
			content = {@Content(schema = @Schema(hidden = true))})
	@GetMapping("/v1/demoAdmin/parasoftVirtualizeServerUrlValidation")
	@ResponseBody
	public ResponseResult<Void> validateParasoftVirtualizeServerUrl(String url) throws VirtualizeServerUrlException {
		ResponseResult<Void> response =
				ResponseResult.getInstance(ResponseResult.STATUS_OK, ResponseResult.MESSAGE_OK);

		parasoftJDBCProxyService.validateVirtualizeServerUrl(url);

		return response;
	}

	@Operation(description = "Obtain MQ properties.")
	@ApiResponse(responseCode = "200", description = "MQ properties were returned.")
	@GetMapping("/v1/demoAdmin/mqProperties")
	@ResponseBody
	public ResponseResult<MQPropertiesResponseDTO> getMQProperties() {

		ResponseResult<MQPropertiesResponseDTO> response =
				ResponseResult.getInstance(ResponseResult.STATUS_OK, ResponseResult.MESSAGE_OK);
		response.setData(globalPreferencesService.getMQProperties());
		return response;
	}

	@Operation(description = "Obtain gRPC properties.")
	@ApiResponse(responseCode = "200", description = "gRPC properties were returned.")
	@GetMapping("/v1/demoAdmin/gRPCProperties")
	@ResponseBody
	public ResponseResult<GRPCPropertiesResponseDTO> getGRPCProperties() {

		ResponseResult<GRPCPropertiesResponseDTO> response =
				ResponseResult.getInstance(ResponseResult.STATUS_OK, ResponseResult.MESSAGE_OK);
		GRPCPropertiesResponseDTO grpcPropertiesResponseDTO = new GRPCPropertiesResponseDTO(gRPConfig.getPort());
		response.setData(grpcPropertiesResponseDTO);
		return response;
	}

	@Operation(description = "Validate Kafka Broker URL.")
	@ApiResponse(responseCode = "200", description = "Valid URL.")
	@ApiResponse(responseCode = "500", description = "Invalid URL.",
			content = {@Content(schema = @Schema(hidden = true))})
	@GetMapping("/v1/demoAdmin/kafkaBrokerUrlValidation")
	@ResponseBody
	public ResponseResult<Void> validateKafkaBrokerUrl() throws KafkaServerIsNotAvailableException {
		ResponseResult<Void> response =
				ResponseResult.getInstance(ResponseResult.STATUS_OK, ResponseResult.MESSAGE_OK);

		globalPreferencesService.validateKafkaBrokerUrl();

		return response;
	}

	@Operation(description = "Validate RabbitMQ server URL.")
	@ApiResponse(responseCode = "200", description = "Valid URL.")
	@ApiResponse(responseCode = "500", description = "Invalid URL.",
			content = {@Content(schema = @Schema(hidden = true))})
	@GetMapping("/v1/demoAdmin/rabbitMQUrlValidation")
	@ResponseBody
	public ResponseResult<Void> validateRabbitMQServerUrl() throws RabbitMQServerIsNotAvailableException {
		ResponseResult<Void> response =
				ResponseResult.getInstance(ResponseResult.STATUS_OK, ResponseResult.MESSAGE_OK);

		globalPreferencesService.validateRabbitMQServerUrl();

		return response;
	}
}
