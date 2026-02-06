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
import app.krista.extensions.devsecops.development.jira.impl.UserDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class UserValidator implements Validator {

    private final UserDao userDao;

    private static final Logger logger = LoggerFactory.getLogger(UserValidator.class);

    public UserValidator(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public Boolean validate(String resourceId, Map<ValidationResource, String> context) {
        try {
            return userDao.isUserExist(resourceId);
        } catch (IllegalCallerException cause) {
            logger.error(cause.getMessage(), cause);
            throw new IllegalCallerException(cause.getMessage(), cause);
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
        return JiraResources.ACCOUNT_ID;
    }

    @Override
    public String getFetchStepMessage() {
        return "Please enter valid Account Id.";
    }

    @Override
    public String getConfirmationStepMessage(String resourceId, Map<ValidationResource, String> context) {
        return String.format("The provided account id: %s does not exist. Please check.", resourceId);
    }

    @Override
    public String getErrMessage(String resourceId) {
        return String.format("Invalid account id : %s", resourceId);
    }
}
