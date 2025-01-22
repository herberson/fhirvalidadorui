/*
 * Copyright (C) 2023 Tarea Gerenciamento Ltda. (contato@tarea.com.br)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.Properties;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.DefaultCaret;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import com.zcage.log.TextAreaAppender;

import br.com.tarea.fhir.FhirCtx;
import br.com.tarea.fhir.msg.Messages;
import br.com.tarea.fhir.msg.MessagesKeys;
import br.com.tarea.fhir.val.ExecutarValidacao;
import br.com.tarea.fhir.val.ExecutarValidacaoException;
import ca.uhn.fhir.util.VersionUtil;
import net.sourceforge.plantuml.utils.Log;

public class FhirValidacaoWindow {
	private static final Logger logger = Logger.getLogger(FhirValidacaoWindow.class);
	
	private JFrame frame;
	private JTextField txtLocalDefs;
	private JButton btnSelDirDef = new JButton(label(MessagesKeys.btnSelDir));
	private JButton btnSelValidar = new JButton(label(MessagesKeys.btnSelectJsonFile));
	private JButton btnSelMsg = new JButton(label(MessagesKeys.btnSelectMessagesFile));
	private JButton btnValidar = new JButton(label(MessagesKeys.btnExecuteValidation));
	private JTextField txtArqJSON;
	private JTextField txtMsg;
    private RSyntaxTextArea txtResValidacao = new RSyntaxTextArea();
    private RSyntaxTextArea txtLog = new RSyntaxTextArea();
	private JCheckBox jScrollLock = new JCheckBox(label(MessagesKeys.chkScrollLock));
	private JCheckBox jLineWrap = new JCheckBox(label(MessagesKeys.chkLineWrap));
	private JCheckBox jApenasFalhas = new JCheckBox(label(MessagesKeys.chkErrorsOnly));
	private JProgressBar progressBar = new JProgressBar(SwingConstants.HORIZONTAL);
    private JComboBox<String> comboBoxFhirVersion = new JComboBox<>();

	static final String fname;
	
	static final Properties cfg;
	
	private static final String disclaimer = ""
			+ "=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=\n"
			+ "Copyright (C) 2023 Tarea Gerenciamento Ltda (contato@tarea.com.br)\n"
			+ "                   https://www.tarea.com.br/\n"
			+ "=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=\n"
			+ "Licensed under the Apache License, Version 2.0 (the \"License\");\n"
			+ "you may not use this file except in compliance with the License.\n"
			+ "You may obtain a copy of the License at\n"
			+ "\n"
			+ "http://www.apache.org/licenses/LICENSE-2.0\n"
			+ "\n"
			+ "Unless required by applicable law or agreed to in writing, \n"
			+ "software distributed under the License is distributed on an \n"
			+ "\"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, \n"
			+ "either express or implied.\n"
			+ "See the License for the specific language governing permissions \n"
			+ "and limitations under the License.\n"
			+ "=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=\n"
			+ "Licenciado sob a Licença Apache, Versão 2.0 (a \"Licença\");\n"
			+ "você não pode usar este arquivo, exceto em conformidade \n"
			+ "com a Licença.\n"
			+ "Você pode obter uma cópia da Licença em\n"
			+ "\n"
			+ "http://www.apache.org/licenses/LICENSE-2.0\n"
			+ "\n"
			+ "A menos que exigido pela lei aplicável ou acordado por \n"
			+ "escrito, o software distribuído sob a Licença é distribuído \n"
			+ "\"COMO ESTÁ\", SEM GARANTIAS OU CONDIÇÕES DE QUALQUER TIPO, \n"
			+ "expressas ou implícitas.\n"
			+ "Consulte a Licença para obter as permissões de controle do\n"
			+ "idioma específico e limitações sob a Licença.\n"
			+ "=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-="
			+ "";
	
	private String disclaimerAsHtml()
	{
		String dsc = disclaimer;
		
		dsc = dsc.replace("https://www.tarea.com.br", StringUtils.repeat("&nbsp;", 19) + "<a href=\"https://www.tarea.com.br\">https://www.tarea.com.br</a>");
		dsc = dsc.replace("Tarea Gerenciamento Ltda", "<a href=\"https://www.tarea.com.br\">Tarea Gerenciamento Ltda</a>");
		dsc = dsc.replace("contato@tarea.com.br", "<a href=\"mailto:contato@tarea.com.br\">contato@tarea.com.br</a>");
		
		dsc = dsc.replace("\n", "<br/>\n");
		dsc = "<html><span style='font-family: monospace, monospace;'>" + dsc;
		dsc += "</span></html>";
		
		return dsc;
	}
	
    static {
		System.out.println(disclaimer);
		
		logger.error("\n" + disclaimer + "\n");
		logger.debug("\n" + disclaimer + "\n");
		logger.fatal("\n" + disclaimer + "\n");
		logger.info("\n" + disclaimer + "\n");
		logger.trace("\n" + disclaimer + "\n");
		logger.warn("\n" + disclaimer + "\n");
		
    	fname = String.format("%s%s%s.cfg", 
    			System.getProperty("user.home"), 
    			System.getProperty("file.separator"), 
    			FhirValidacaoWindow.class.getSimpleName()
    	);
    	
    	cfg = new Properties();
    	
        try {
        	File f = new File(fname);
        	if (f.exists()) {
        		cfg.load(new FileInputStream(f));
        	}
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
    }
    
    private static String label(final MessagesKeys key)
    {
    	return Messages.getKey(key);
    }
    
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FhirValidacaoWindow window = new FhirValidacaoWindow();
					window.frame.setVisible(true);
					
					window.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					window.frame.setTitle("Tarea - " + label(MessagesKeys.windowTitle));
					window.frame.setLocationRelativeTo(null);
					window.frame.setSize(new Dimension(845, 710));
					window.frame.setMinimumSize(new Dimension(400, 600));
            		
                    try {
                    	final int x = Integer.parseInt(cfg.getProperty("pos.x"));
                    	final int y = Integer.parseInt(cfg.getProperty("pos.y"));
                    	final Point p = new Point(x, y);
                    	
                    	if (isLocationInScreenBounds(p)) {
                    		window.frame.setLocation(p);
                    	}
                    } catch (NumberFormatException e) {
                    	logger.info(e.getMessage());
                    }
                    
                    try {
                    	final int h = Integer.parseInt(cfg.getProperty("dim.h", "710"));
                    	final int w = Integer.parseInt(cfg.getProperty("dim.w", "845"));
                    	
                    	window.frame.setSize(new Dimension(w, h));
                    } catch (NumberFormatException e) {
                    	logger.info(e.getMessage());
                    }
                    
                    window.frame.addWindowListener(new WindowAdapter() {
                        public void windowClosing(WindowEvent e) {
                        	cfg.put("def.path", window.txtLocalDefs.getText());
                        	cfg.put("json.path", window.txtArqJSON.getText());
                        	cfg.put("msg.path", window.txtMsg.getText());
                        	cfg.put("scroll.lock", window.jScrollLock.isSelected() + "");
                        	cfg.put("line.wrap", window.jLineWrap.isSelected() + "");
                        	cfg.put("apenas.falhas", window.jApenasFalhas.isSelected() + "");
                        	cfg.put("pos.x", ((int) window.frame.getLocationOnScreen().getX()) + "");
                        	cfg.put("pos.y", ((int) window.frame.getLocationOnScreen().getY()) + "");
                        	cfg.put("dim.h", ((int) window.frame.getSize().getHeight()) + "");
                        	cfg.put("dim.w", ((int) window.frame.getSize().getWidth()) + "");
                        	cfg.put("fhir.version", window.comboBoxFhirVersion.getSelectedItem() + "");
                        	
                        	try {
								cfg.store(new FileOutputStream(fname), "");
							} catch (Exception e1) {
								logger.info(e1.getMessage());
							}
                        }
                    });
                    
                    setupLog4JAppender(window.txtLog);
					
				} catch (Exception e) {
					logger.info(e.getMessage());
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public FhirValidacaoWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		BorderLayout borderLayout = (BorderLayout) frame.getContentPane().getLayout();
		borderLayout.setHgap(5);
		frame.setBounds(new Rectangle(20, 23, 0, 0));
		frame.setBounds(100, 100, 999, 844);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new EmptyBorder(10, 10, 10, 10));
		frame.getContentPane().add(panel_2, BorderLayout.NORTH);
		
		panel_2.setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		panel_2.add(panel, BorderLayout.NORTH);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] {0, 999, 0};
		gbl_panel.rowHeights = new int[] {26, 26};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, 0.0};
		gbl_panel.rowWeights = new double[]{0.0, 0.0};
		panel.setLayout(gbl_panel);
		
		JLabel lblNewLabel_1_2 = new JLabel(label(MessagesKeys.labelBtnSelDir));
		lblNewLabel_1_2.setMinimumSize(new Dimension(170, 16));
		lblNewLabel_1_2.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblNewLabel_1_2 = new GridBagConstraints();
		gbc_lblNewLabel_1_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1_2.gridx = 0;
		gbc_lblNewLabel_1_2.gridy = 0;
		panel.add(lblNewLabel_1_2, gbc_lblNewLabel_1_2);
		
		txtLocalDefs = new JTextField();
		txtLocalDefs.setToolTipText(label(MessagesKeys.hintBtnSelDir));
		GridBagConstraints gbc_txtLocalDefs = new GridBagConstraints();
		gbc_txtLocalDefs.fill = GridBagConstraints.BOTH;
		gbc_txtLocalDefs.insets = new Insets(0, 0, 5, 5);
		gbc_txtLocalDefs.gridx = 1;
		gbc_txtLocalDefs.gridy = 0;
		panel.add(txtLocalDefs, gbc_txtLocalDefs);
		txtLocalDefs.setColumns(10);
		GridBagConstraints gbc_btnSelDirDef = new GridBagConstraints();
		gbc_btnSelDirDef.insets = new Insets(0, 0, 5, 0);
		gbc_btnSelDirDef.gridx = 2;
		gbc_btnSelDirDef.gridy = 0;
		panel.add(btnSelDirDef, gbc_btnSelDirDef);
		
		JLabel lblNewLabel_1 = new JLabel(label(MessagesKeys.labelBtnSelectJsonFile));
		lblNewLabel_1.setMinimumSize(new Dimension(170, 16));
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 1;
		panel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		txtArqJSON = new JTextField();
		txtArqJSON.setToolTipText(label(MessagesKeys.hintBtnSelectJsonFile));
		GridBagConstraints gbc_txtArqJSON = new GridBagConstraints();
		gbc_txtArqJSON.insets = new Insets(0, 0, 5, 5);
		gbc_txtArqJSON.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtArqJSON.gridx = 1;
		gbc_txtArqJSON.gridy = 1;
		panel.add(txtArqJSON, gbc_txtArqJSON);
		txtArqJSON.setColumns(10);
		
		GridBagConstraints gbc_btnSelValidar = new GridBagConstraints();
		gbc_btnSelValidar.insets = new Insets(0, 0, 5, 0);
		gbc_btnSelValidar.gridx = 2;
		gbc_btnSelValidar.gridy = 1;
		panel.add(btnSelValidar, gbc_btnSelValidar);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(5, 0, 0, 0));
		panel_2.add(panel_1, BorderLayout.SOUTH);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_3 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_3.getLayout();
		flowLayout.setVgap(0);
		panel_1.add(panel_3, BorderLayout.WEST);
		
		panel_3.add(jScrollLock);
		jScrollLock.setHorizontalAlignment(SwingConstants.LEFT);
		
		panel_3.add(jLineWrap);
		panel_3.add(jApenasFalhas);
		
        jLineWrap.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	jLineWrap_actionPerformed(e);
            }
        });
        jScrollLock.setSelected(Boolean.parseBoolean(cfg.getProperty("scroll.lock", "false")));
        jLineWrap.setSelected(Boolean.parseBoolean(cfg.getProperty("line.wrap", "false")));
        jApenasFalhas.setSelected(Boolean.parseBoolean(cfg.getProperty("apenas.falhas", "false")));
        
        JLabel lblNewLabel_3 = new JLabel(label(MessagesKeys.cmbFhirVersion));
        panel_3.add(lblNewLabel_3);
        
        comboBoxFhirVersion.setModel(new DefaultComboBoxModel<String>(new String[] {"R4", "R4B", "R5"}));
        panel_3.add(comboBoxFhirVersion);
        
		JPanel panel_4 = new JPanel();
		panel_1.add(panel_4, BorderLayout.CENTER);
		panel_4.setLayout(new BorderLayout(0, 0));
		panel_4.add(btnValidar, BorderLayout.EAST);
		
		btnValidar.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        btnValidar_actionPerformed(e);
		    }
		});
		
		panel_4.add(progressBar, BorderLayout.CENTER);
		
		JPanel panel_5 = new JPanel();
		panel_5.setBorder(new EmptyBorder(0, 10, 10, 10));
		frame.getContentPane().add(panel_5, BorderLayout.CENTER);
		panel_5.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_6 = new JPanel();
		panel_5.add(panel_6, BorderLayout.NORTH);
		panel_6.setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel_2 = new JLabel(label(MessagesKeys.txtValidationMessages));
		panel_6.add(lblNewLabel_2, BorderLayout.WEST);
		lblNewLabel_2.setHorizontalAlignment(SwingConstants.LEFT);
		
		JScrollPane scrollPane = new JScrollPane();
        panel_5.add(scrollPane, BorderLayout.CENTER);
		
		JPanel panel_5_1 = new JPanel();
		frame.getContentPane().add(panel_5_1, BorderLayout.SOUTH);
		panel_5_1.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_7 = new JPanel();
		panel_5_1.add(panel_7, BorderLayout.NORTH);
		GridBagLayout gbl_panel_7 = new GridBagLayout();
		gbl_panel_7.columnWidths = new int[] {0, 999, 0};
		gbl_panel_7.rowHeights = new int[] {26, 0};
		gbl_panel_7.columnWeights = new double[]{0.0, 1.0, 0.0};
		gbl_panel_7.rowWeights = new double[]{0.0, 0.0};
		panel_7.setLayout(gbl_panel_7);
		
		JLabel lblNewLabel_1_1 = new JLabel(label(MessagesKeys.labelBtnSelectMessagesFile));
		GridBagConstraints gbc_lblNewLabel_1_1 = new GridBagConstraints();
		gbc_lblNewLabel_1_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1_1.gridx = 0;
		gbc_lblNewLabel_1_1.gridy = 0;
		panel_7.add(lblNewLabel_1_1, gbc_lblNewLabel_1_1);
		lblNewLabel_1_1.setMinimumSize(new Dimension(170, 16));
		lblNewLabel_1_1.setHorizontalAlignment(SwingConstants.RIGHT);
		
		txtMsg = new JTextField();
		GridBagConstraints gbc_txtMsg = new GridBagConstraints();
		gbc_txtMsg.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtMsg.insets = new Insets(0, 0, 5, 5);
		gbc_txtMsg.gridx = 1;
		gbc_txtMsg.gridy = 0;
		panel_7.add(txtMsg, gbc_txtMsg);
		txtMsg.setToolTipText(label(MessagesKeys.hintBtnSelectMessagesFile));
		txtMsg.setColumns(10);
		txtMsg.setText(cfg.getProperty("msg.path", ""));
		
        GridBagConstraints gbc_btnSelMsg = new GridBagConstraints();
        gbc_btnSelMsg.insets = new Insets(0, 0, 5, 0);
        gbc_btnSelMsg.gridx = 2;
        gbc_btnSelMsg.gridy = 0;
        panel_7.add(btnSelMsg, gbc_btnSelMsg);

        btnSelMsg.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btnSelMsg_actionPerformed(e);
            }
        });
		
		JPanel pnLog = new JPanel();
		panel_5_1.add(pnLog, BorderLayout.SOUTH);
		panel_5_1.setBorder(new EmptyBorder(0, 10, 10, 10));
		pnLog.setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel_2_1 = new JLabel(label(MessagesKeys.labelLogPanel));
		pnLog.add(lblNewLabel_2_1, BorderLayout.NORTH);
		lblNewLabel_2_1.setHorizontalAlignment(SwingConstants.LEFT);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		pnLog.add(scrollPane_1, BorderLayout.CENTER);
		pnLog.setPreferredSize(new Dimension(200, 200));
        scrollPane.setViewportView(txtResValidacao);
        
        txtResValidacao.setTabSize(2);
        txtResValidacao.setFont(new Font("Monospaced", 0, 12));
        txtResValidacao.setMargin(new Insets(2, 2, 2, 2));
        txtResValidacao.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON_WITH_COMMENTS);
        txtResValidacao.setMinimumSize(new Dimension(200,  200));
        
		scrollPane_1.setViewportView(txtLog);
		txtLog.setTabSize(2);
		txtLog.setFont(new Font("Monospaced", 0, 10));
		txtLog.setMargin(new Insets(2, 2, 2, 2));
		
		JPanel panel_8 = new JPanel();
		pnLog.add(panel_8, BorderLayout.SOUTH);
		panel_8.setLayout(new BorderLayout(0, 0));
		
		JButton btnNewButton = new JButton(label(MessagesKeys.btnHelp));
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent event) {
				JOptionPane.showMessageDialog(frame, disclaimerAsHtml(), frame.getTitle(), 
						JOptionPane.INFORMATION_MESSAGE);
			}
		});
		panel_8.add(btnNewButton, BorderLayout.EAST);
		
		JPanel panel_9 = new JPanel();
		panel_8.add(panel_9, BorderLayout.WEST);
		panel_9.setLayout(new BorderLayout(0, 0));
		
        final String urihapifhir = "https://hapifhir.io/";
        final String urihl7fhir = "https://www.hl7.org/fhir/";
		
		JButton btnNewButton_1 = new JButton("HL7 FHIR");
		panel_9.add(btnNewButton_1, BorderLayout.WEST);
		btnNewButton_1.setToolTipText(urihl7fhir);
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent event) {
				if (Desktop.isDesktopSupported()) 
				{
					try
					{
						Desktop.getDesktop().browse(new URI(urihl7fhir));
					}
					catch (Exception e)
					{
						Log.error(e.getMessage());
					}
				}
				logger.info(btnNewButton_1.getText() + " - " +  urihl7fhir);
			}
		});
		JButton lblNewLabel = new JButton("HAPI-FHIR v" + VersionUtil.getVersion() + "");
		panel_9.add(lblNewLabel, BorderLayout.EAST);
		lblNewLabel.setToolTipText(urihapifhir);
		lblNewLabel.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent event) {
				if (Desktop.isDesktopSupported()) 
				{
					try
					{
						Desktop.getDesktop().browse(new URI(urihapifhir));
					}
					catch (Exception e)
					{
						Log.error(e.getMessage());
					}
				}
				logger.info(lblNewLabel.getText() + " - " +  urihapifhir);
			}
		});
		
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setStringPainted(true);
        progressBar.setString(label(MessagesKeys.labelProgressBar));
        
        btnSelDirDef.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btnSelDirDef_actionPerformed(e);
            }
        });
        
        btnSelValidar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btnSelValidar_actionPerformed(e);
            }
        });

        txtLocalDefs.setText(cfg.getProperty("def.path", ""));
        txtArqJSON.setText(cfg.getProperty("json.path", ""));
        
        comboBoxFhirVersion.setFont(new Font("Monospaced", Font.BOLD, 12));
        
        comboBoxFhirVersion.setSelectedItem((String)cfg.getProperty("fhir.version", "R4"));
        
        final JTextField[] txts = new JTextField[] {txtLocalDefs, txtArqJSON, txtMsg};
        
        for (JTextField item : txts) {
        	item.setFont(new Font("Monospaced", 0, 11));
        }
	}

    private void jLineWrap_actionPerformed(ActionEvent e) {
    	final JTextArea[] wrap = new JTextArea[] {txtResValidacao, txtLog};
    	final boolean isSelected = jLineWrap.isSelected();
    	for (JTextArea item : wrap) {
    		item.setWrapStyleWord(isSelected);
    		item.setLineWrap(isSelected);
    	}
    }
    
    private void btnSelDirDef_actionPerformed(ActionEvent e) {
        JFileChooser chooser;

        chooser = new JFileChooser();
        
        if (StringUtils.isNotBlank(txtLocalDefs.getText())) {
        	chooser.setCurrentDirectory(new File(txtLocalDefs.getText()));
        } else {
        	chooser.setCurrentDirectory(new File("."));
        }
        
        chooser.setDialogTitle(label(MessagesKeys.titleBtnSelDir));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        //
        // disable the "All files" option.
        //
        chooser.setAcceptAllFileFilterUsed(false);
        //
        if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
        	txtLog.setText("");
        	txtResValidacao.setText("");
            txtLocalDefs.setText(chooser.getCurrentDirectory().getPath());
        } else {
            logger.debug("FHIR definitions folder not selected. Is gonna use HAPI-FHIR defaults.");
        }
    }

    private void btnSelValidar_actionPerformed(ActionEvent e) {
        JFileChooser chooser;
        
        chooser = new JFileChooser();
        chooser.setFileFilter(new FileFilter() 
        {
			@Override
			public boolean accept(final File pathname) 
			{
				if (pathname.isDirectory())
				{
					return true;
				}
				else
				{
					return pathname.getName().toLowerCase().endsWith(".json");
				}
			}

			@Override
			public String getDescription() 
			{
				return "*.json";
			}
		});
        
        if (StringUtils.isNotBlank(txtArqJSON.getText())) {
        	chooser.setCurrentDirectory(new File(txtArqJSON.getText()));
        } else {
        	chooser.setCurrentDirectory(new File("."));
        }
        
        chooser.setDialogTitle(label(MessagesKeys.titleBtnSelectJsonFile));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        //
        // disable the "All files" option.
        //
        chooser.setAcceptAllFileFilterUsed(false);
        //
        if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            txtArqJSON.setText(chooser.getSelectedFile().getPath());
        	txtLog.setText("");
        	txtResValidacao.setText("");
        } else {
            logger.error("FHIR JSON file for validation not selected.");
        }
    }

    private void btnSelMsg_actionPerformed(ActionEvent e) {
        JFileChooser chooser;
        
        chooser = new JFileChooser();
        
        if (StringUtils.isNotBlank(txtMsg.getText())) {
        	chooser.setCurrentDirectory(new File(txtMsg.getText()));
        } else {
        	chooser.setCurrentDirectory(new File("."));
        }
    
        chooser.setDialogTitle(label(MessagesKeys.titleBtnSelectMessagesFile));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        //
        // disable the "All files" option.
        //
        chooser.setAcceptAllFileFilterUsed(false);
        
        //
        if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            txtMsg.setText(chooser.getSelectedFile().getPath());
        	txtLog.setText("");
        	txtResValidacao.setText("");
        } else {
            logger.error("File to write messages not selected.");
        }
    }

    private void btnValidar_actionPerformed(ActionEvent e) {
    	final String localDef, jsonSample, jsonMessages;
    	final boolean apenasFalhas, scrollLock;
    	
    	txtLog.setText("");
    	txtResValidacao.setText("");
    	
    	apenasFalhas = jApenasFalhas.isSelected();
    	
    	final DefaultCaret caret = (DefaultCaret)txtResValidacao.getCaret();
    	
    	jLineWrap_actionPerformed(null);
    	
    	if (jScrollLock.isSelected()) {
    		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
    		scrollLock = true;
    	} else {
    		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    		scrollLock = false;
    	}
    	
    	FhirCtx.use((String)comboBoxFhirVersion.getSelectedItem());
    	
    	try {
    		localDef = txtLocalDefs.getText();
    		jsonSample = txtArqJSON.getText();
    		jsonMessages = txtMsg.getText();
    		
    		new Thread(() -> {
    			final JComponent[] disable = new JComponent[] {txtLocalDefs, txtArqJSON, txtMsg, 
    					btnSelDirDef, btnSelValidar, btnSelMsg, btnValidar, jScrollLock, jLineWrap, 
    					jApenasFalhas, comboBoxFhirVersion};
    			
    			btnValidar.setVisible(false);
    			progressBar.setVisible(true);
    			
    			for (JComponent cmp : disable) {
    				cmp.setEnabled(false);
    			}
    			
                try {
                	ExecutarValidacao.executar(localDef, jsonSample, jsonMessages, apenasFalhas, txtResValidacao, scrollLock);
                } catch (Exception e2) {
                	logger.error(e2);
                	
                	Throwable e2cause = e2.getCause();
                	
                	if (e2 instanceof com.google.gson.JsonParseException || e2cause instanceof com.google.gson.JsonParseException) {
                		txtResValidacao.setText(e2.getMessage());
                	}
                	
                	if (e2 instanceof com.fasterxml.jackson.core.JsonParseException || e2cause instanceof com.fasterxml.jackson.core.JsonParseException) {
                		txtResValidacao.setText(e2.getMessage());
                	}
                	
                	if (e2 instanceof ExecutarValidacaoException || e2cause instanceof ExecutarValidacaoException) {
                		txtResValidacao.setText(e2.getMessage());
                	}
                	
                	if (e2 instanceof ca.uhn.fhir.rest.server.exceptions.InternalErrorException || e2cause instanceof ca.uhn.fhir.rest.server.exceptions.InternalErrorException)
                	{
                		txtResValidacao.setText(ExceptionUtils.getStackTrace(e2));
                	}
                }
                btnValidar.setVisible(true);
                progressBar.setVisible(false);
                
            	caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

    			for (JComponent cmp : disable) {
    				cmp.setEnabled(true);
    			}
            }).start();
    		
		} catch (Exception e1) {
			caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
			logger.error(e1);
		}
    }
    
    /**
     * Verifies if the given point is visible on the screen.
     * 
     * @param   location     The given location on the screen.
     * @return           True if the location is on the screen, false otherwise.
     */
	public static boolean isLocationInScreenBounds(final Point location) {
		// Check if the location is in the bounds of one of the graphics devices.
		final GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		final GraphicsDevice[] graphicsDevices = graphicsEnvironment.getScreenDevices();
		final Rectangle graphicsConfigurationBounds = new Rectangle();

		// Iterate over the graphics devices.
		for (int j = 0; j < graphicsDevices.length; j++) {

			// Get the bounds of the device.
			final GraphicsDevice graphicsDevice = graphicsDevices[j];
			graphicsConfigurationBounds.setRect(graphicsDevice.getDefaultConfiguration().getBounds());

			// Is the location in this bounds?
			graphicsConfigurationBounds.setRect(graphicsConfigurationBounds.x, 
					graphicsConfigurationBounds.y,
					graphicsConfigurationBounds.width, 
					graphicsConfigurationBounds.height);
			
			if (graphicsConfigurationBounds.contains(location.x, location.y)) {
				// The location is in this screengraphics.
				return true;
			}
		}
		
		return false;
	}
    
    protected static void setupLog4JAppender(final JTextArea jTextArea) {
        TextAreaAppender.setTextArea(jTextArea);
    }
}
