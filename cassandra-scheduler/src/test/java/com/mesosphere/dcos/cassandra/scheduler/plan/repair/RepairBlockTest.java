package com.mesosphere.dcos.cassandra.scheduler.plan.repair;

import com.mesosphere.dcos.cassandra.common.tasks.CassandraDaemonTask;
import com.mesosphere.dcos.cassandra.common.tasks.CassandraTask;
import com.mesosphere.dcos.cassandra.common.tasks.repair.RepairContext;
import com.mesosphere.dcos.cassandra.common.tasks.repair.RepairTask;
import com.mesosphere.dcos.cassandra.scheduler.client.SchedulerClient;
import com.mesosphere.dcos.cassandra.scheduler.offer.ClusterTaskOfferRequirementProvider;
import com.mesosphere.dcos.cassandra.scheduler.tasks.CassandraTasks;
import org.apache.mesos.Protos;
import org.apache.mesos.offer.OfferRequirement;
import org.apache.mesos.scheduler.plan.Status;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

public class RepairBlockTest {
    public static final String CLEANUP_NODE_0 = "repair-node-0";
    public static final String NODE_0 = "node-0";
    @Mock
    private ClusterTaskOfferRequirementProvider provider;
    @Mock
    private CassandraTasks cassandraTasks;
    @Mock
    private SchedulerClient client;
    public static final RepairContext CONTEXT = RepairContext.create(Collections.emptyList(),
            Collections.emptyList(), Collections.emptyList());

    @Before
    public void beforeEach() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testInitial() {
        Mockito.when(cassandraTasks.get(CLEANUP_NODE_0)).thenReturn(Optional.empty());
        final RepairContext context = RepairContext.create(Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList());
        final RepairBlock block = RepairBlock.create(
                NODE_0,
                cassandraTasks,
                provider,
                context);
        Assert.assertEquals(CLEANUP_NODE_0, block.getName());
        Assert.assertEquals(NODE_0, block.getDaemon());
        Assert.assertTrue(block.isPending());
    }

    @Test
    public void testComplete() {
        final CassandraTask mockCassandraTask = Mockito.mock(CassandraTask.class);
        Mockito.when(mockCassandraTask.getState()).thenReturn(Protos.TaskState.TASK_FINISHED);
        Mockito.when(cassandraTasks.get(CLEANUP_NODE_0))
                .thenReturn(Optional.ofNullable(mockCassandraTask));
        final RepairContext context = RepairContext.create(Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList());
        final RepairBlock block = RepairBlock.create(
                NODE_0,
                cassandraTasks,
                provider,
                context);
        Assert.assertEquals(CLEANUP_NODE_0, block.getName());
        Assert.assertEquals(NODE_0, block.getDaemon());
        Assert.assertTrue(block.isComplete());
    }

    @Test
    public void testTaskStartAlreadyCompleted() throws Exception {
        final CassandraDaemonTask daemonTask = Mockito.mock(CassandraDaemonTask.class);
        Mockito.when(cassandraTasks.get(CLEANUP_NODE_0)).thenReturn(Optional.empty());
        final HashMap<String, CassandraDaemonTask> map = new HashMap<>();
        map.put(NODE_0, null);
        Mockito.when(cassandraTasks.getDaemons()).thenReturn(map);
        final RepairContext context = RepairContext.create(Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList());

        final RepairTask task = Mockito.mock(RepairTask.class);
        Mockito.when(task.getSlaveId()).thenReturn("1234");
        Mockito
                .when(cassandraTasks.getOrCreateRepair(daemonTask, CONTEXT))
                .thenReturn(task);

        final RepairBlock block = RepairBlock.create(
                NODE_0,
                cassandraTasks,
                provider,
                context);

        final OfferRequirement requirement = Mockito.mock(OfferRequirement.class);
        Mockito.when(provider.getUpdateOfferRequirement(Mockito.any())).thenReturn(requirement);
        Assert.assertNull(block.start());
        Assert.assertTrue(block.isComplete());
    }

    @Test
    public void testTaskStart() throws Exception {
        final CassandraDaemonTask daemonTask = Mockito.mock(CassandraDaemonTask.class);
        Mockito.when(cassandraTasks.get(CLEANUP_NODE_0)).thenReturn(Optional.empty());
        final HashMap<String, CassandraDaemonTask> map = new HashMap<>();
        map.put(NODE_0, daemonTask);
        Mockito.when(cassandraTasks.getDaemons()).thenReturn(map);

        final RepairTask task = Mockito.mock(RepairTask.class);
        Mockito.when(task.getSlaveId()).thenReturn("1234");
        Mockito
                .when(cassandraTasks.getOrCreateRepair(daemonTask, CONTEXT))
                .thenReturn(task);

        final RepairBlock block = RepairBlock.create(
                NODE_0,
                cassandraTasks,
                provider,
                CONTEXT);

        final OfferRequirement requirement = Mockito.mock(OfferRequirement.class);
        Mockito.when(provider.getUpdateOfferRequirement(Mockito.any())).thenReturn(requirement);
        Assert.assertNotNull(block.start());
        Assert.assertTrue(block.isInProgress());
    }
}
