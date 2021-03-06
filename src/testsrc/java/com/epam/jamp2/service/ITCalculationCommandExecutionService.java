package com.epam.jamp2.service;

import com.epam.jamp2.model.CalculationCommand;
import com.epam.jamp2.model.Operation;
import com.epam.jamp2.model.Value;
import com.epam.jamp2.rest.FixerioServiceProxyConfiguration;
import com.epam.jamp2.service.impl.CalculationCommandExecutionServiceImpl;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContextManager;

import java.math.BigDecimal;
import java.util.Collection;

import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.number.BigDecimalCloseTo.closeTo;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
@ContextConfiguration(classes = {TestConfig.class, FixerioServiceProxyConfiguration.class })
public class ITCalculationCommandExecutionService {

    // Performs the same job as @RunWith(SpringJUnit4ClassRunner.class)
    private static TestContextManager testContextManager;

    @Autowired
    private CalculationCommandExecutionServiceImpl target;

    private CalculationCommand parameter;
    private Value expected;

    public ITCalculationCommandExecutionService(
            Value leftOperand,
            Value rightOperand,
            Operation operation,
            String resultCurrencyCode,
            Value expected)
    {
        this.parameter = new CalculationCommand(leftOperand, rightOperand, operation, resultCurrencyCode);
        this.expected = expected;
    }

    @Parameters(name = "{index}: integration test on calculate (parameter: {0}, {1}, {2}, {3}) should return = {4}")
    public static Collection<Object[]> data()
    {
        return CalculationCommandExecutionServiceImplScenario.data();
    }

    @BeforeClass
    public static void setUpSpringContext()
    {
        testContextManager = new TestContextManager(ITCalculationCommandExecutionService.class);
    }

    @Before
    public void injectDependencies() throws Exception
    {
        testContextManager.prepareTestInstance(this);
    }

    @Test
    public void test_calculate()
    {
        Value result = target.calculate(parameter);
        BigDecimal errorTolerance = expected.getValue().multiply(BigDecimal.valueOf(0.20)); // i.e. ±15% for currency fluctuations
        assertThat((result), hasProperty("value", closeTo(expected.getValue(), errorTolerance)));
        assertThat(result.getCurrencyCode(), equalTo(expected.getCurrencyCode()));
    }

}