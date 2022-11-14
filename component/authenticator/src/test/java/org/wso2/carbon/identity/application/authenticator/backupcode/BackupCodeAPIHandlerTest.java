/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authenticator.backupcode;

import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authenticator.backupcode.exception.BackupCodeClientException;
import org.wso2.carbon.identity.application.authenticator.backupcode.exception.BackupCodeException;
import org.wso2.carbon.identity.application.authenticator.backupcode.util.BackupCodeUtil;

import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.powermock.api.mockito.PowerMockito.*;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.wso2.carbon.identity.application.authenticator.backupcode.constants.BackupCodeAuthenticatorConstants.Claims.BACKUP_CODES_CLAIM;
import static org.wso2.carbon.identity.application.authenticator.backupcode.constants.BackupCodeAuthenticatorConstants.Claims.BACKUP_CODES_ENABLED_CLAIM;

@PrepareForTest({BackupCodeAPIHandler.class, BackupCodeUtil.class, MultitenantUtils.class})
public class BackupCodeAPIHandlerTest extends PowerMockTestCase {

    private final String username = "test1";
    private final String tenantAwareUserName = "test1";

    @Mock
    UserStoreManager userStoreManager;

    @Test(dataProvider = "backupCodesCountData")
    public void testGetRemainingBackupCodesCount(Map<String, String> userClaimValues, String username,
                                                 int remainingBackupCodesCount)
            throws UserStoreException, BackupCodeException {

        mockStatic(BackupCodeUtil.class);
        mockStatic(MultitenantUtils.class);
        when(MultitenantUtils.getTenantAwareUsername(username)).thenReturn(tenantAwareUserName);
        when(BackupCodeUtil.getUserStoreManagerOfUser(username)).thenReturn(userStoreManager);
        when(userStoreManager.getUserClaimValues(tenantAwareUserName, new String[]{BACKUP_CODES_CLAIM},
                null)).thenReturn(userClaimValues);
        if (username.equals("test1")) {
            assertEquals(remainingBackupCodesCount, BackupCodeAPIHandler.getRemainingBackupCodesCount(username));
        } else {
            assertEquals(0, BackupCodeAPIHandler.getRemainingBackupCodesCount("test2"));
        }
    }

    @Test(expectedExceptions = BackupCodeClientException.class)
    public void testGetRemainingBackupCodesCountNullUserName() throws BackupCodeException {

        BackupCodeAPIHandler.getRemainingBackupCodesCount("");
        BackupCodeAPIHandler.getRemainingBackupCodesCount(null);
    }

    @DataProvider(name = "backupCodesCountData")
    public Object[][] dataForRemainingBackupCodesCount() {

        Map<String, String> testClaims1 = new HashMap<>();
        testClaims1.put(BACKUP_CODES_CLAIM, "");
        testClaims1.put(BACKUP_CODES_ENABLED_CLAIM, "true");

        Map<String, String> testClaims2 = new HashMap<>();
        testClaims2.put(BACKUP_CODES_CLAIM, "234563");
        testClaims2.put(BACKUP_CODES_ENABLED_CLAIM, "true");

        Map<String, String> testClaims3 = new HashMap<>();
        testClaims3.put(BACKUP_CODES_CLAIM, null);
        testClaims3.put(BACKUP_CODES_ENABLED_CLAIM, "true");

        Map<String, String> testClaims4 = new HashMap<>();
        testClaims4.put(BACKUP_CODES_CLAIM, "234563,467064");
        testClaims4.put(BACKUP_CODES_ENABLED_CLAIM, "true");

        return new Object[][]{{testClaims1, username, 0}, {testClaims2, username, 1}, {testClaims3, username, 0},
                {testClaims4, username, 2}};
    }

    @Test(dataProvider = "generateBackupCodesData")
    public void testGenerateBackupCodes(List<String> backupCodes) throws BackupCodeException {

        mockStatic(BackupCodeUtil.class);
        mockStatic(MultitenantUtils.class);
        String tenantDomain = "test.domain";
        when(MultitenantUtils.getTenantDomain(username)).thenReturn(tenantDomain);
        when(MultitenantUtils.getTenantAwareUsername(username)).thenReturn(username);
        when(BackupCodeUtil.getUserStoreManagerOfUser(username)).thenReturn(userStoreManager);
        when(BackupCodeUtil.generateBackupCodes(tenantDomain)).thenReturn(backupCodes);
        assertEquals(backupCodes, BackupCodeAPIHandler.generateBackupCodes(username));
    }

    @Test(expectedExceptions = BackupCodeClientException.class)
    public void testGenerateBackupCodesNullUserName() throws BackupCodeException {

        BackupCodeAPIHandler.generateBackupCodes("");
        BackupCodeAPIHandler.generateBackupCodes(null);
    }

    @DataProvider(name = "generateBackupCodesData")
    public Object[][] dataForGeneratingBackupCodes() {

        List<String> backupCodes1 = new ArrayList<>();

        List<String> backupCodes2 = new ArrayList<>();
        backupCodes2.add("123567");
        backupCodes2.add("456789");

        List<String> backupCodes3 = new ArrayList<>();
        backupCodes3.add("");
        backupCodes3.add(" ");
        backupCodes3.add(null);

        return new Object[][]{{backupCodes1}, {backupCodes2}, {backupCodes3}};
    }

    @Test
    public void testDeleteBackupCodes() throws BackupCodeException {

        mockStatic(BackupCodeUtil.class);
        mockStatic(MultitenantUtils.class);
        when(MultitenantUtils.getTenantAwareUsername(username)).thenReturn(tenantAwareUserName);
        when(BackupCodeUtil.getUserStoreManagerOfUser(username)).thenReturn(userStoreManager);
        assertTrue(BackupCodeAPIHandler.deleteBackupCodes(username));
    }

    @Test(expectedExceptions = BackupCodeClientException.class)
    public void testDeleteBackupCodesNullUserName() throws BackupCodeException {

        BackupCodeAPIHandler.deleteBackupCodes("");
        BackupCodeAPIHandler.deleteBackupCodes(null);
    }
}
