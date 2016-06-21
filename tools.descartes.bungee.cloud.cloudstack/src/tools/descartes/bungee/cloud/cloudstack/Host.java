package tools.descartes.bungee.cloud.cloudstack;

public class Host {
	
	private int CPU = 8;
	private int memory = 24000;
	private final String id;
	
	public Host(String id){
		this.id = id;
	}
	
	public int getCPU() {
		return CPU;
	}

	public void setCPU(int cPU) {
		CPU = cPU;
	}

	public int getMemory() {
		return memory;
	}

	public void setMemory(int memory) {
		this.memory = memory;
	}

	public String getId() {
		return id;
	}
	
	public void removeCPU(int amount){
		this.CPU = this.CPU + amount;
	}
	
	public void addCPU(int amount){
		this.CPU = this.CPU - amount;
	}
	
	public void removeMemory(int amount){
		this.memory = this.memory + amount;
	}
	
	public void addMemory(int amount){
		this.memory = this.memory - amount;
	}

	public void addVM(VirtualMachine vm) {
		addMemory(vm.getMemory());
		addCPU(vm.getCpunumber());
	}
	
	public void removeVM(VirtualMachine vm) {
		removeMemory(vm.getMemory());
		removeCPU(vm.getCpunumber());
	}
	
	@Override
	public String toString(){
		return getId()+", free CPU: "+getCPU()+", free memory: "+getMemory();
	}
}
