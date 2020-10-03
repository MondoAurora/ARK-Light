package ark.dock;

import java.util.Map;
import java.util.TreeMap;

import dust.gen.DustGenUtils;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ArkDockMind implements ArkDockConsts {

	public static class ArkDockMindContext<MindType extends ArkDockMind> extends DustEntityContext {
		public final MindType mind;

		public ArkDockMindContext(MindType mind_) {
			super();
			this.mind = mind_;
		}
	}

	public interface MindAgent<MindCtx extends ArkDockMindContext> extends ArkDockAgent<MindCtx> {
	}

	public static abstract class BaseAgent<MindCtx extends ArkDockMindContext> implements MindAgent<MindCtx> {
		private MindCtx ctx;
		
		private DustEntity eDef;
		
		public DustEntity getDef() {
			return eDef;
		}
		
		public ArkDockMind getMind() {
			return ctx.mind;
		}

		@Override
		public DustResultType agentAction(DustAgentAction action) throws Exception {
			switch ( action ) {
			case INIT:
				eDef = getActionCtx().entity;
				return DustResultType.ACCEPT;
			default:
				break;
			}
			return DustResultType.ACCEPT_PASS;
		}
		
		@Override
		public void setActionCtx(MindCtx ctx) {
			this.ctx = ctx;
		}

		@Override
		public MindCtx getActionCtx() {
			return ctx;
		}
	}
	
	public static abstract class WrapAgent<WrapType> extends BaseAgent<ArkDockMindContext> {
		private WrapType wrapOb;

		public WrapType getWrapOb() {
			return wrapOb;
		}
		
		protected abstract WrapType createWrapOb();

		@Override
		public DustResultType agentAction(DustAgentAction action) throws Exception {
			DustResultType ret = super.agentAction(action);

			if  ( DustGenUtils.isReject(ret) ) {
				return ret;
			}
			
			switch ( action ) {
			case INIT:
				wrapOb = createWrapOb();
				break;
			default:
				break;
			}
			
			return ret;
		}
	}

	public final ArkDockModelMeta modMeta;
	public final ArkDockModel modMain;
	
	public final ArkDockTokens.Idea tokIdea;
	public final ArkDockTokens.Model tokModel;
	public final ArkDockTokens.Generic tokGeneric;
	public final ArkDockTokens.Native tokNative;
	public final ArkDockTokens.Text tokText;

	private DustEntity eUnitMain;

	private final Map<String, MindAgent<?>> agents = new TreeMap<>();
	
	public ArkDockMind(ArkDockModel modMain_) {
		modMain = modMain_;
		modMeta = modMain.getMeta();
		
		tokIdea = modMeta.tokIdea;
		tokModel = modMeta.tokModel;
		tokGeneric = modMeta.tokGeneric;
		tokText = modMeta.tokText;
		
		tokNative = new ArkDockTokens.Native(modMeta);
	}
	
	public final DustEntity getUnit() {
		return eUnitMain;
	}
	
	public void setUnit(DustEntity eUnit) {
		this.eUnitMain = eUnit;
	}

	public final DustEntity getEntity(DustEntity eType, String id, boolean createIfMissing) {
		return modMain.getEntity(eUnitMain, eType, id, createIfMissing);
	}

	public final DustEntity getAgent(String agentName) {
		return getEntity(tokIdea.eTypeAgent, agentName, false);
	}

	protected ArkDockMindContext createCtx() {
		return new ArkDockMindContext(this);
	}

	protected MindAgent createAgent(String agentName) throws Exception {
		DustEntity eAgent = getAgent(agentName);

		String cName = modMain.getMember(eAgent, tokNative.eBinaryId, null, null);
		Class<?> c = Class.forName(cName);
		
		MindAgent agent = (MindAgent) c.newInstance();

		return agent;
	}

	public final DustResultType initAgent(String agentName) throws Exception {
		if ( agentName.startsWith("-") ) {
			return DustResultType.REJECT;
		}
		
		MindAgent agent = createAgent(agentName);

		DustEntity eAgent = getAgent(agentName);

		modMain.setMember(eAgent, tokNative.eNativeCollType, tokIdea.eConstColltypeOne, null);
		modMain.setMember(eAgent, tokNative.eNativeValueOne, agent, null);

		DustResultType res = sendAgent(agent, DustAgentAction.INIT, eAgent, null, null, null);

		agents.put(agentName, agent);

		return res;
	}

	public final DustResultType sendAgent(String agentName, DustAgentAction cmd) throws Exception {
		return sendAgent(agentName, cmd, null, null, null, null);
	}

	public final DustResultType sendAgent(String agentName, DustAgentAction cmd, DustEntity e, DustEntity m, Object val,
			Object key) throws Exception {
		MindAgent agent = agents.get(agentName);
		return sendAgent(agent, cmd, e, m, val, key);
	}

	public final DustResultType sendAgent(MindAgent agent, DustAgentAction cmd, DustEntity e, DustEntity m, Object val,
			Object key) throws Exception {
		ArkDockMindContext ctx = createCtx();
		
		ctx.entity = e;
		ctx.member = m;
		ctx.value = val;
		ctx.key = key;

		try {
			agent.setActionCtx(ctx);
			return agent.agentAction(cmd);
		} finally {
			agent.setActionCtx(null);
		}
	}
}
