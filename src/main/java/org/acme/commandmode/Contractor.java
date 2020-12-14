package org.acme.commandmode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CsvRecord(separator = ",", skipFirstLine = false)
public class Contractor {

    @DataField(pos=1)
    String licenseNo;

    @DataField(pos=2)
    String lastUpdate;

    @DataField(pos=3)
    String businessName;

    @DataField(pos=4)
    String businessName2;

    @DataField(pos=5)
    String fullBusinessName;

    @DataField(pos=6)
    String mailingAddress;

    @DataField(pos=7)
    String city;

    @DataField(pos=8)
    String state;

    @DataField(pos=9)
    String county;

    @DataField(pos=10)
    String zIPCode;

    @DataField(pos=11)
    String country;

    @DataField(pos=12)
    String businessPhone;

    @DataField(pos=13)
    String businessType;

    @DataField(pos=14)
    String issueDate;

    @DataField(pos=15)
    String reissueDate;

    @DataField(pos=16)
    String expirationDate;

    @DataField(pos=17)
    String inactivationDate;

    @DataField(pos=18)
    String reactivationDate;

    @DataField(pos=19)
    String pendingSuspension;

    @DataField(pos=20)
    String pendingClassRemoval;

    @DataField(pos=21)
    String pendingClassReplace;

    @DataField(pos=22)
    String primaryStatus;

    @DataField(pos=23)
    String secondaryStatus;

    @DataField(pos=24)
    String classifications;

    @DataField(pos=25)
    String asbestosReg;

    @DataField(pos=26)
    String workersCompCoverageType;

    @DataField(pos=27)
    String wCInsuranceCompany;

    @DataField(pos=28)
    String wCPolicyNumber;

    @DataField(pos=29)
    String wCEffectiveDate;

    @DataField(pos=30)
    String wCExpirationDate;

    @DataField(pos=31)
    String wCCancellationDate;

    @DataField(pos=32)
    String wCSuspendDate;

    @DataField(pos=33)
    String cBSuretyCompany;

    @DataField(pos=34)
    String cBNumber;

    @DataField(pos=35)
    String cBEffectiveDate;

    @DataField(pos=36)
    String cBCancellationDate;

    @DataField(pos=37)
    String cBAmount;

    @DataField(pos=38)
    String wBSuretyCompany;

    @DataField(pos=39)
    String wBNumber;

    @DataField(pos=40)
    String wBEffectiveDate;

    @DataField(pos=41)
    String wBCancellationDate;

    @DataField(pos=42)
    String wBAmount;

    @DataField(pos=43)
    String dBSuretyCompany;

    @DataField(pos=44)
    String dBNumber;

    @DataField(pos=45)
    String dBEffectiveDate;

    @DataField(pos=46)
    String dBCancellationDate;

    @DataField(pos=47)
    String dBAmount;

    @DataField(pos=48)
    String dateRequired;

    @DataField(pos=49)
    String discpCaseRegion;

    @DataField(pos=50)
    String dBBondReason;

    @DataField(pos=51)
    String dBCaseNo;

    @DataField(pos=52)
    String nameTP2;
}
