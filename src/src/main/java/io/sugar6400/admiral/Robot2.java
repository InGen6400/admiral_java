// ��C�Q�[����N���C�A���g�v���O����Robot.java
// ���̃v���O������,�C�Q�[���̃N���C�A���g�v���O�����ł�
// ���߂�ꂽ�菇�ŊC�Q�[�����v���C���܂�
// �g����java Robot �ڑ���T�[�o�A�h���X�Q�[���Q���Җ�
// �N����,�w�肵���T�[�o�Ɛڑ���,�����I�ɃQ�[�����s���܂�
// �N����,�w��񐔂̌J��Ԃ��̌�,logout���܂�
// ���̃v���O������logout�R�}���h������܂���
// �v���O������r���Œ�~����ɂ�,�ȉ��̎菇�𓥂�ł�������
// �i�P�j�R���g���[��C ����͂���Robot�v���O�������~���܂�
// �i�Q�jT1.java�v���O�����Ȃ�,�ʂ̃N���C�A���g���g����Robot�Ɠ������O��login���܂�
// �i�R�jlogout���܂�
// �ʃN���C�A���g�����logout��Ƃ��ȗ������,�T�[�o��ɏ�񂪎c���Ă��܂��܂�

// ���C�u�����̗��p
import java.net.*;// �l�b�g���[�N�֘A
import java.io.*;
import java.util.*;

// Robot�N���X
public class Robot2 {
	enum Dir{RIGHT, DOWN, LEFT, UP, NONE}
	// ���{�b�g�̓���^�C�~���O���K�肷��ϐ�sleeptime
	int sleeptime = 510;
	// ���{�b�g��logout����܂ł̎��Ԃ��K�肷��ϐ�timeTolive
	int timeTolive = 50 ;

	String name;
	// ���̍X�V���̈ړ�
	Dir[] nextMove = new Dir[2];
	int my_x, my_y;

	static Vector<int[]> energy_v; // �R���^���N�̈ʒu��񃊃X�g
	static Hashtable<String, Ship> userTable = null;
	// �R���X�g���N�^
	public Robot2 (String[] args)
	{
		userTable = new Hashtable<>();
		energy_v = new Vector<>();
		nextMove[0] = Dir.NONE;
		nextMove[1] = Dir.NONE;
		login(args[0],args[1]) ;

		while(true){
			Update();
			try{
				Thread.sleep(sleeptime);
			}catch(Exception e){
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	public void Update(){
		try{
			Reload();
			// �����d�ݕt���G�l���M�[�ʏ��Ƀ\�[�g
			energy_v.sort(new Comparator<int[]>() {
				@Override
				public int compare(int[] o1, int[] o2) {
					return Integer.compare(o1[3], o2[3]);
				}
			});
			/* �f�o�b�O�o�͗p
			for (int[] energy: energy_v){
				System.out.print(energy[3]+",");
			}*/
			nextMove[0] = Dir.NONE;
			nextMove[1] = Dir.NONE;
		}catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}
	}


	private void Move(Dir dir){
		switch (dir){
			case DOWN:
				out.println("down");
				break;
			case UP:
				out.println("up");
				break;
			case RIGHT:
				out.println("right");
				break;
			case LEFT:
				out.println("left");
				break;
		}
	}

	public void Reload(){

		Move(nextMove[0]);
		Move(nextMove[1]);
		if(nextMove[0] != Dir.NONE || nextMove[1] != Dir.NONE)
			out.flush();

		// �T�[�o��stat�R�}���h�𑗕t��,�Ֆʂ̗l�q�Ȃǂ̏��𓾂܂�
		out.println("stat");
		out.flush();

		try {
			String line = in.readLine();// �T�[�o����̓��͂̓ǂݍ���

			//ship_info����n�܂�D�̏��̐擪�s��T���܂�
			while (!"ship_info".equalsIgnoreCase(line))
				line = in.readLine();

			// �D�̏��ship_info�̕\��
			// ship_info�̓s���I�h�݂̂̍s�ŏI���ł�
			line = in.readLine();
			while (!".".equals(line)){
				StringTokenizer st = new StringTokenizer(line);
				// ���O��ǂݎ��܂�
				String obj_name = st.nextToken().trim();

				// �D�̈ʒu���W��ǂݎ��܂�
				int x = Integer.parseInt(st.nextToken()) ;
				int y = Integer.parseInt(st.nextToken()) ;
				int point = Integer.parseInt(st.nextToken()) ;

				// �D�ꗗ�ɓo�^
				if(userTable.containsKey(obj_name)){
					userTable.get(obj_name).x = x;
					userTable.get(obj_name).y = y;
					userTable.get(obj_name).point = point;
				}else{
					userTable.put(obj_name, new Ship(x, y, point));
				}

				// �����̑D�Ȃ�
				if(obj_name.equals(name)){
					my_x = x;
					my_y = y;
				}
				// ���̂P�s��ǂݎ��܂�
				line = in.readLine();
			}

			// energy_info����n�܂�,�R���^���N�̏���҂��󂯂܂�
			while (!"energy_info".equalsIgnoreCase(line))
				line = in.readLine();

			energy_v.clear();
			// �R���^���N�̏��energy_info�̕\��
			// energy_info�̓s���I�h�݂̂̍s�ŏI���ł�
			line = in.readLine();
			while (!".".equals(line)){
				StringTokenizer st = new StringTokenizer(line);

				// �R���^���N�̈ʒu���W��ǂݎ��܂�
				int x   = Integer.parseInt(st.nextToken()) ;
				int y   = Integer.parseInt(st.nextToken()) ;
				int ene = Integer.parseInt(st.nextToken()) ;
				int[] e = new int[4];
				// �G�l���M�[�ꗗ�ɓo�^
				e[0] = x;
				e[1] = y;
				e[2] = ene;
				// 1,2,3,4�̂ǂ�ł�����؂�鐔12 -> �K��������
				energy_v.addElement(e);

				// ���̂P�s��ǂݎ��܂�
				line = in.readLine();
			}
		}catch (Exception e){
			e.printStackTrace();
			System.exit(1);
		}
	}

	// login�֘A�̃I�u�W�F�N�g
	Socket server;// �Q�[���T�[�o�Ƃ̐ڑ��\�P�b�g
	int port = 10000;// �ڑ��|�[�g
	BufferedReader in;// ���̓X�g���[��
	PrintWriter out;// �o�̓X�g���[��

	// login���\�b�h
	// �T�[�o�ւ�login�������s���܂�
	void login(String host, String name){
		try {
			// �T�[�o�Ƃ̐ڑ�
			this.name = name;
			server = new Socket(host, port);
			in = new BufferedReader(new InputStreamReader(
			  server.getInputStream()));
			out = new PrintWriter(server.getOutputStream());

			// login�R�}���h�̑��t
			out.println("login " + name);
			out.flush();
		}catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}
	}

	void logout(){
		try {
			// logout����
			out.println("logout") ;
			out.flush();
			server.close();
		}catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}
	}

	// main���\�b�h
	// Robot���N�����܂�
	public static void main(String[] args){
		new Robot2(args);
	}
}

class Ship {
	// �D�̈ʒu���W
	public int x;
	public int y;
	// �l�������R���^���N�̌�
	public int point = 0;

	// �R���X�g���N�^
	// �����ʒu���Z�b�g���܂�
	public Ship(int x, int y, int point){
		this.x = x;
		this.y = y;
		this.point = point;
	}
}
