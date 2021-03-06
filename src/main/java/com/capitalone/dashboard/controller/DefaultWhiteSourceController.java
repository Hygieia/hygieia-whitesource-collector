package com.capitalone.dashboard.controller;


import com.capitalone.dashboard.collector.DefaultWhiteSourceClient;
import com.capitalone.dashboard.misc.HygieiaException;
import com.capitalone.dashboard.model.WhiteSourceRefreshRequest;
import com.capitalone.dashboard.model.WhiteSourceRequest;
import com.capitalone.dashboard.util.CommonConstants;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Objects;


import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;


@RestController
public class DefaultWhiteSourceController {

    private final DefaultWhiteSourceClient defaultWhiteSourceClient;
    private final HttpServletRequest httpServletRequest;

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultWhiteSourceController.class);

    @Autowired
    public DefaultWhiteSourceController(HttpServletRequest httpServletRequest, DefaultWhiteSourceClient defaultWhiteSourceClient) {
        this.httpServletRequest = httpServletRequest;
        this.defaultWhiteSourceClient = defaultWhiteSourceClient;
    }

    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 401, message = "BAD REQUEST"),
            @ApiResponse(code = 403, message = "forbidden( Unauthorized)"),
            @ApiResponse(code = 500, message = "System Internal Error") })
    @RequestMapping(value = "/project-alerts", method = POST,
            consumes = "application/json", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> setProjectVitalsAndAlerts(@Valid @RequestBody WhiteSourceRequest request) throws HygieiaException {
        request.setClientReference(httpServletRequest.getHeader(CommonConstants.HEADER_CLIENT_CORRELATION_ID));
        String requester = httpServletRequest.getHeader(CommonConstants.HEADER_API_USER);
        String response = defaultWhiteSourceClient.process(request);
        LOGGER.info("correlation_id="+request.getClientReference() +", application=hygieia, service=whitesource-collector, uri=" + httpServletRequest.getRequestURI()+", requester="+requester+
                ", response_status=success, response_code=" +HttpStatus.CREATED.value()+", response_status_message="+response);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @RequestMapping(value = "/refresh", method = POST,
            consumes = "application/json", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> refresh(@Valid @RequestBody WhiteSourceRefreshRequest request) throws HygieiaException {
        if (Objects.isNull(request) ||Objects.isNull(request.getOrgName()) ||Objects.isNull(request.getProjectToken()) ){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Required fields are null");
        }
        defaultWhiteSourceClient.refresh(request.getOrgName(),request.getProjectToken());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body("Updated Whitesource component:: OrgName="+request.getOrgName()+ ", projectToken="+request.getProjectToken());
    }

}
