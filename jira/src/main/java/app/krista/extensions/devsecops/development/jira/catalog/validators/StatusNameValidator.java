/*
 * JIRA SERVICE MANAGEMENT Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>. 
 */

package app.krista.extensions.devsecops.development.jira.catalog.validators;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extensions.devsecops.development.jira.catalog.extresp.JiraResources;
import app.krista.extensions.devsecops.development.jira.impl.TicketInfoDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.stream.Collectors;

public class StatusNameValidator implements Validator {

    private final TicketInfoDao ticketInfoDao;

    private static final Logger logger = LoggerFactory.getLogger(StatusNameValidator.class);

    public StatusNameValidator(TicketInfoDao ticketInfoDao) {
        this.ticketInfoDao = ticketInfoDao;
    }

    @Override
    public Boolean validate(String resourceId, Map<ValidationResource, String> context) {
        try {
            return ticketInfoDao.getTransitions(context.get(ValidationResource.TICKET_ID))
                    .stream().map(t -> t.toName).collect(Collectors.toList()).contains(resourceId);
        } catch (MustAuthorizeException cause) {
            logger.error(cause.getMessage(), cause);
            throw cause;
        } catch (IllegalCallerException cause) {
            logger.error(cause.getMessage(), cause);
            throw new IllegalCallerException(cause.getMessage(), cause);
        } catch (Exception cause) {
            logger.error(cause.getMessage(), cause);
            return false;
        }
    }

    @Override
    public String getFetchFieldName() {
        return JiraResources.STATUS_NAME;
    }

    @Override
    public String getFetchStepMessage() {
        return "Please enter valid Status Name. The current one is not valid for this issue.";
    }

    @Override
    public String getConfirmationStepMessage(String resourceId, Map<ValidationResource, String> context) {
        String validStatus = ticketInfoDao.getTransitions(context.get(ValidationResource.TICKET_ID))
                .stream().map(t -> t.toName).collect(Collectors.joining(","));
        return String.format("The provided status name: %s is not valid for this issue. Available status for this issue are : %s Please check.", resourceId, validStatus);
    }

    @Override
    public String getErrMessage(String resourceId) {
        return String.format("Invalid status name : %s", resourceId);
    }
}
