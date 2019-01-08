// ｢海ゲーム｣クライアントプログラムRobot.java
// このプログラムは,海ゲームのクライアントプログラムです
// 決められた手順で海ゲームをプレイします
// 使い方java Robot 接続先サーバアドレスゲーム参加者名
// 起動後,指定したサーバと接続し,自動的にゲームを行います
// 起動後,指定回数の繰り返しの後,logoutします
// このプログラムはlogoutコマンドがありません
// プログラムを途中で停止するには,以下の手順を踏んでください
// （１）コントロールC を入力してRobotプログラムを停止します
// （２）T1.javaプログラムなど,別のクライアントを使ってRobotと同じ名前でloginします
// （３）logoutします
// 別クライアントからのlogout作業を省略すると,サーバ上に情報が残ってしまいます

// ライブラリの利用
import java.net.*;// ネットワーク関連
import java.io.*;
import java.util.*;

// Robotクラス
public class Robot2 {
	enum Dir{RIGHT, DOWN, LEFT, UP, NONE}
	// ロボットの動作タイミングを規定する変数sleeptime
	int sleeptime = 510;
	// ロボットがlogoutするまでの時間を規定する変数timeTolive
	int timeTolive = 50 ;

	String name;
	// 次の更新時の移動
	Dir[] nextMove = new Dir[2];
	int my_x, my_y;

	static Vector<int[]> energy_v; // 燃料タンクの位置情報リスト
	static Hashtable<String, Ship> userTable = null;
	// コンストラクタ
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
			// 距離重み付けエネルギー量順にソート
			energy_v.sort(new Comparator<int[]>() {
				@Override
				public int compare(int[] o1, int[] o2) {
					return Integer.compare(o1[3], o2[3]);
				}
			});
			/* デバッグ出力用
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

		// サーバにstatコマンドを送付し,盤面の様子などの情報を得ます
		out.println("stat");
		out.flush();

		try {
			String line = in.readLine();// サーバからの入力の読み込み

			//ship_infoから始まる船の情報の先頭行を探します
			while (!"ship_info".equalsIgnoreCase(line))
				line = in.readLine();

			// 船の情報ship_infoの表示
			// ship_infoはピリオドのみの行で終了です
			line = in.readLine();
			while (!".".equals(line)){
				StringTokenizer st = new StringTokenizer(line);
				// 名前を読み取ります
				String obj_name = st.nextToken().trim();

				// 船の位置座標を読み取ります
				int x = Integer.parseInt(st.nextToken()) ;
				int y = Integer.parseInt(st.nextToken()) ;
				int point = Integer.parseInt(st.nextToken()) ;

				// 船一覧に登録
				if(userTable.containsKey(obj_name)){
					userTable.get(obj_name).x = x;
					userTable.get(obj_name).y = y;
					userTable.get(obj_name).point = point;
				}else{
					userTable.put(obj_name, new Ship(x, y, point));
				}

				// 自分の船なら
				if(obj_name.equals(name)){
					my_x = x;
					my_y = y;
				}
				// 次の１行を読み取ります
				line = in.readLine();
			}

			// energy_infoから始まる,燃料タンクの情報を待ち受けます
			while (!"energy_info".equalsIgnoreCase(line))
				line = in.readLine();

			energy_v.clear();
			// 燃料タンクの情報energy_infoの表示
			// energy_infoはピリオドのみの行で終了です
			line = in.readLine();
			while (!".".equals(line)){
				StringTokenizer st = new StringTokenizer(line);

				// 燃料タンクの位置座標を読み取ります
				int x   = Integer.parseInt(st.nextToken()) ;
				int y   = Integer.parseInt(st.nextToken()) ;
				int ene = Integer.parseInt(st.nextToken()) ;
				int[] e = new int[4];
				// エネルギー一覧に登録
				e[0] = x;
				e[1] = y;
				e[2] = ene;
				// 1,2,3,4のどれでも割り切れる数12 -> 必ず整数に
				energy_v.addElement(e);

				// 次の１行を読み取ります
				line = in.readLine();
			}
		}catch (Exception e){
			e.printStackTrace();
			System.exit(1);
		}
	}

	// login関連のオブジェクト
	Socket server;// ゲームサーバとの接続ソケット
	int port = 10000;// 接続ポート
	BufferedReader in;// 入力ストリーム
	PrintWriter out;// 出力ストリーム

	// loginメソッド
	// サーバへのlogin処理を行います
	void login(String host, String name){
		try {
			// サーバとの接続
			this.name = name;
			server = new Socket(host, port);
			in = new BufferedReader(new InputStreamReader(
			  server.getInputStream()));
			out = new PrintWriter(server.getOutputStream());

			// loginコマンドの送付
			out.println("login " + name);
			out.flush();
		}catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}
	}

	void logout(){
		try {
			// logout処理
			out.println("logout") ;
			out.flush();
			server.close();
		}catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}
	}

	// mainメソッド
	// Robotを起動します
	public static void main(String[] args){
		new Robot2(args);
	}
}

class Ship {
	// 船の位置座標
	public int x;
	public int y;
	// 獲得した燃料タンクの個数
	public int point = 0;

	// コンストラクタ
	// 初期位置をセットします
	public Ship(int x, int y, int point){
		this.x = x;
		this.y = y;
		this.point = point;
	}
}
