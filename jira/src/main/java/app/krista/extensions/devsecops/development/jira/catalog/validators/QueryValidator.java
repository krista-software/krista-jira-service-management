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
import app.krista.extensions.devsecops.development.jira.impl.IssueDao;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static app.krista.extensions.devsecops.development.jira.util.JiraConstants.ERRORS;


public class QueryValidator implements Validator {

    private String error;
    private final IssueDao issueDao;
    private final RequestContext requestContext;
    private final AuthorizationContext authorizationContext;

    private static final Logger logger = LoggerFactory.getLogger(QueryValidator.class);

    public QueryValidator(IssueDao issueDao, RequestContext requestContext, AuthorizationContext authorizationContext) {
        this.issueDao = issueDao;
        this.requestContext = requestContext;
        this.authorizationContext = authorizationContext;
    }

    @Override
    public Boolean validate(String resourceId, Map<ValidationResource, String> context) {
        try {
            return isJQLQueryValid(resourceId);
        } catch (MustAuthorizeException cause) {
            logger.error(cause.getMessage(), cause);
            throw cause;
        } catch (Exception cause) {
            logger.error(cause.getMessage(), cause);
            return false;
        }
    }

    @Override
    public String getFetchFieldName() {
        return JiraConstants.JQL_QUERY;
    }

    @Override
    public String getFetchStepMessage() {
        return "Please enter valid JQL Query Type.";
    }

    @Override
    public String getConfirmationStepMessage(String resourceId, Map<ValidationResource, String> context) {
        return String.format("The provided JQL Query : %s is invalid. Please check. %s", resourceId, error);
    }

    @Override
    public String getErrMessage(String resourceId) {
        return String.format("Invalid JQL Query : %s", error);
    }

    private boolean isJQLQueryValid(String jQLQuery) {
        boolean invokeAsUser = requestContext.invokeAsUser();
        if (invokeAsUser) {
            issueDao.validateCredentials();
        }
        String invokerId = requestContext.getInvokerId();
        String accountID = authorizationContext.getAuthorizedAccount().getAccountId();

        Map<String, String> queryData = issueDao.fetchErrorsInJQLQuery(invokeAsUser, accountID, invokerId, jQLQuery);
        if (queryData.containsKey(ERRORS)) {
            error = queryData.get(ERRORS);
            return false;
        } else {
            return true;
        }
    }
}
